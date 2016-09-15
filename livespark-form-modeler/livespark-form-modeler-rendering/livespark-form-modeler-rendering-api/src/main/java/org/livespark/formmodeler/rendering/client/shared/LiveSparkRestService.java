/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.livespark.formmodeler.rendering.client.shared;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.livespark.formmodeler.rendering.client.shared.query.QueryCriteria;

public interface LiveSparkRestService<M> {

   @POST
   @Consumes("application/json")
   @Produces("application/json") M create(M model);

   @Path("load")
   @GET
   @Produces("application/json") List<M> load();

   @Path("load/{start}/{end}")
   @GET
   @Produces("application/json") List<M> load( @PathParam( "start" ) int start, @PathParam( "end" ) int end);

   @Path("list")
   @POST
   @Consumes("application/json")
   @Produces("application/json") List<M> list( QueryCriteria criteria );


   @Path("update")
   @PUT
   @Consumes("application/json")
   @Produces("application/json") Boolean update(M model);

   @Path("delete")
   @DELETE
   @Consumes("application/json")
   @Produces("application/json") Boolean delete(M model);
}
