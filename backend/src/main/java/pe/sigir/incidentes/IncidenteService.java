package pe.sigir.incidentes;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.transaction.Transactional;
import pe.sigir.auditoria.Auditor;
import pe.sigir.compartido.*;
import pe.sigir.eventos.EventoIncidente;
import pe.sigir.eventos.TipoEvento;
import pe.sigir.incidentes.IncidenteDtos.*;
import pe.sigir.revision.DecisionRevision;
import pe.sigir.revision.HistorialRevisiones;
import pe.sigir.revision.RevisionDao;
import pe.sigir.roles.Permiso;
import pe.sigir.usuarios.Usuario;
import pe.sigir.usuarios.UsuarioDao;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@ApplicationScoped
public class IncidenteService {

    private final IncidenteDao dao;
    private final RevisionDao revisiones;
    private final HistorialRevisiones historial;
    private final UsuarioDao usuarios;
    private final Auditor auditor;
    private final UsuarioActual actual;
    private final SecurityIdentity identidad;
    private final Event<EventoIncidente> eventos;

    public IncidenteService(IncidenteDao dao, RevisionDao revisiones, HistorialRevisiones historial,
                            UsuarioDao usuarios, Auditor auditor, UsuarioActual actual,
                            SecurityIdentity identidad, Event<EventoIncidente> eventos) {
        this.dao = dao;
        this.revisiones = revisiones;
        this.historial = historial;
        this.usuarios = usuarios;
        this.auditor = auditor;
        this.actual = actual;
        this.identidad = identidad;
        this.eventos = eventos;
    }

    public Pagina<IncidenteResumen> buscar(FiltroIncidentes filtro) {
        return dao.buscar(filtro).map(IncidenteResumen::de);
    }

    public Incidente obtener(Long id) {
        return dao.buscarPorId(id).orElseThrow(() -> new NoEncontradoException("Incidente no encontrado"));
    }

    public IncidenteDetalle detalle(Long id) {
        return detalle(obtener(id));
    }

    @Transactional
    public IncidenteDetalle crear(IncidenteRequest p) {
        validarFechas(p);
        Incidente i = new Incidente();
        i.codigo = "INC-%d-%04d".formatted(LocalDate.now().getYear(), dao.siguienteCorrelativo());
        i.reportadoPor = yo();
        copiar(p, i);
        dao.guardar(i);
        auditor.registrar(actual.email(), "INCIDENTE_CREADO", "INCIDENTE", String.valueOf(i.id), i.codigo + " " + i.titulo);
        publicar(TipoEvento.INCIDENTE_CREADO, i, null);
        return detalle(i);
    }

    @Transactional
    public IncidenteDetalle actualizar(Long id, IncidenteRequest p) {
        Incidente i = obtener(id);
        exigirAutor(i);
        if (!i.esEditable()) {
            throw new NegocioException("El incidente no se puede editar en estado " + i.estado);
        }
        validarFechas(p);
        copiar(p, i);
        auditor.registrar(actual.email(), "INCIDENTE_ACTUALIZADO", "INCIDENTE", String.valueOf(i.id), i.codigo);
        publicar(TipoEvento.INCIDENTE_ACTUALIZADO, i, null);
        return detalle(i);
    }

    @Transactional
    public IncidenteDetalle enviar(Long id) {
        Incidente i = obtener(id);
        exigirAutor(i);
        i.enviarARevision();
        historial.registrar(i.id, yo(), DecisionRevision.ENVIADO, null);
        auditor.registrar(actual.email(), "INCIDENTE_ENVIADO", "INCIDENTE", String.valueOf(i.id), i.codigo);
        publicar(TipoEvento.INCIDENTE_ENVIADO, i, null);
        return detalle(i);
    }

    // ---------- apoyo ----------

    public IncidenteDetalle detalle(Incidente i) {
        return new IncidenteDetalle(i.id, i.codigo, i.titulo, i.descripcion, i.tipo, i.severidad, i.estado,
                i.regulador, i.area, i.fechaOcurrencia, i.fechaDeteccion, i.fechaLimiteReporte,
                i.estaVencido(LocalDate.now()), i.impacto, i.medidas, UsuarioRef.de(i.reportadoPor),
                i.creadoEn, i.actualizadoEn,
                revisiones.historialDe(i.id).stream().map(HistorialItem::de).toList(),
                acciones(i));
    }

    /** Qué puede hacer el usuario actual con este incidente (la UI muestra solo esos botones). */
    Set<String> acciones(Incidente i) {
        Set<String> permisos = identidad.getRoles();
        boolean autor = i.reportadoPor.id.equals(actual.id());
        Set<String> a = new LinkedHashSet<>();
        if (i.esEditable() && autor && permisos.contains(Permiso.INCIDENTES_EDITAR)) {
            a.add("editar");
            a.add("enviar");
        }
        if (i.estado == EstadoIncidente.EN_REVISION && !autor && permisos.contains(Permiso.REVISION_APROBAR)) {
            a.add("aprobar");
            a.add("rechazar");
        }
        if (i.estado == EstadoIncidente.APROBADO && permisos.contains(Permiso.REVISION_APROBAR)) {
            a.add("cerrar");
        }
        return a;
    }

    public Usuario yo() {
        return usuarios.buscarPorId(actual.id()).orElseThrow(() -> new NoEncontradoException("Usuario no encontrado"));
    }

    public void publicar(TipoEvento tipo, Incidente i, String comentario) {
        eventos.fire(EventoIncidente.de(tipo, i, actual.id(), actual.email(), comentario));
    }

    private void exigirAutor(Incidente i) {
        if (!i.reportadoPor.id.equals(actual.id())) {
            throw new AccesoDenegadoException("Solo quien registró el incidente puede modificarlo");
        }
    }

    private static void validarFechas(IncidenteRequest p) {
        if (p.fechaDeteccion().isBefore(p.fechaOcurrencia())) {
            throw new PeticionInvalidaException("La fecha de detección no puede ser anterior a la de ocurrencia");
        }
    }

    private static void copiar(IncidenteRequest p, Incidente i) {
        i.titulo = p.titulo().trim();
        i.descripcion = p.descripcion().trim();
        i.tipo = p.tipo();
        i.severidad = p.severidad();
        i.regulador = p.regulador();
        i.area = p.area();
        i.fechaOcurrencia = p.fechaOcurrencia();
        i.fechaDeteccion = p.fechaDeteccion();
        i.fechaLimiteReporte = p.severidad().fechaLimite(p.fechaDeteccion());
        i.impacto = p.impacto();
        i.medidas = p.medidas();
    }
}
