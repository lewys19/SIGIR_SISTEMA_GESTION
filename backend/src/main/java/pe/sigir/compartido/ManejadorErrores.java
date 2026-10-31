package pe.sigir.compartido;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import pe.sigir.auth.CredencialesInvalidasException;
import pe.sigir.auth.DemasiadosIntentosException;

/** Traduce las excepciones de negocio a respuestas HTTP con {"mensaje": "..."}. */
public class ManejadorErrores {

    @ServerExceptionMapper
    public Response credencialesInvalidas(CredencialesInvalidasException e) {
        return error(Response.Status.UNAUTHORIZED, e.getMessage());
    }

    @ServerExceptionMapper
    public Response demasiadosIntentos(DemasiadosIntentosException e) {
        return Response.status(429).type(MediaType.APPLICATION_JSON)
                .header("Retry-After", e.minutos() * 60)
                .entity(new ErrorResponse(e.getMessage())).build();
    }

    @ServerExceptionMapper
    public Response negocio(NegocioException e) {
        return error(Response.Status.CONFLICT, e.getMessage());
    }

    @ServerExceptionMapper
    public Response noEncontrado(NoEncontradoException e) {
        return error(Response.Status.NOT_FOUND, e.getMessage());
    }

    @ServerExceptionMapper
    public Response accesoDenegado(AccesoDenegadoException e) {
        return error(Response.Status.FORBIDDEN, e.getMessage());
    }

    @ServerExceptionMapper
    public Response conflictoDeVersion(jakarta.persistence.OptimisticLockException e) {
        return error(Response.Status.CONFLICT, "Otro usuario modificó este registro. Recarga e inténtalo de nuevo");
    }

    @ServerExceptionMapper
    public Response peticionInvalida(PeticionInvalidaException e) {
        return error(Response.Status.BAD_REQUEST, e.getMessage());
    }

    private static Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(mensaje)).build();
    }
}
