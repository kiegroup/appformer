package org.livespark.formmodeler.rendering.client.shared;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Created by pefernan on 6/25/15.
 */
public interface LiveSparkRestService<M extends FormModel> {

   @POST
   @Consumes("application/json")
   @Produces("application/json") M create( M model );

   @Path("load")
   @GET
   @Produces("application/json") List<M> load();

   @Path("update")
   @PUT
   @Consumes("application/json")
   @Produces("application/json") Boolean update( M model );

   @Path("delete")
   @DELETE
   @Consumes("application/json")
   @Produces("application/json") Boolean delete( M model );
}
