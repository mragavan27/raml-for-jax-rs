/*
 * Copyright 2013-2018 (c) MuleSoft, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.raml.jaxrs.generator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.raml.jaxrs.generator.ramltypes.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jean-Philippe Belanger on 12/4/16. Just potential zeroes and ones
 */
public class ResourceUtils {

  public static void fillInBodiesAndResponses(GResource resource,
                                              Multimap<GMethod, GRequest> incomingBodies, Multimap<GMethod, GResponse> responses) {


    for (GMethod method : resource.methods()) {

      if (method.body().size() == 0) {
        incomingBodies.put(method, null);
      } else {
        for (GRequest typeDeclaration : method.body()) {

          incomingBodies.put(method, typeDeclaration);
        }
      }

      if (method.responses().size() == 0) {
        incomingBodies.put(method, null);
      } else {
        for (GResponse response : method.responses()) {

          responses.put(method, response);
        }
      }
    }

  }

  public static List<GParameter> accumulateUriParameters(GResource resource) {

    Set<String> seenHere = extractSeen(new HashSet<String>(), resource);

    List<GParameter> parameters = new ArrayList<>();
    parameters.addAll(Lists.reverse(FluentIterable.from(resource.uriParameters()).toList()));

    while (resource.parentResource() != null) {

      resource = resource.parentResource();
      Set<String> seenInParent = extractSeen(seenHere, resource);
      final Set<String> finalSeenHere = seenHere;
      parameters.addAll(Lists.reverse(FluentIterable.from(resource.uriParameters()).filter(new Predicate<GParameter>() {

        @Override
        public boolean apply(@Nullable GParameter gParameter) {
          return !finalSeenHere.contains(gParameter.name());
        }
      }).toList()));

      seenHere = seenInParent;
    }

    Collections.reverse(parameters);

    return parameters;
  }

  private static ImmutableSet<String> extractSeen(Set<String> seen, GResource resource) {
    return FluentIterable.from(resource.uriParameters())
        .transform(new Function<GParameter, String>() {

          @Nullable
          @Override
          public String apply(@Nullable GParameter gParameter) {
            return gParameter.name();
          }
        }).append(seen).toSet();
  }

}
