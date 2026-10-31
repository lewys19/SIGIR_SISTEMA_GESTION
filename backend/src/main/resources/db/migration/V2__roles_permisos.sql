CREATE TABLE rol_permisos (
    rol     VARCHAR(20) NOT NULL,
    permiso VARCHAR(40) NOT NULL,
    PRIMARY KEY (rol, permiso)
);

-- Matriz inicial (editable desde el módulo Roles/Permisos)
INSERT INTO rol_permisos (rol, permiso) VALUES
  ('ADMIN','dashboard:ver'),('ADMIN','incidentes:ver'),('ADMIN','incidentes:crear'),('ADMIN','incidentes:editar'),
  ('ADMIN','revision:aprobar'),('ADMIN','monitoreo:ver'),('ADMIN','reportes:ver'),('ADMIN','usuarios:gestionar'),
  ('ADMIN','roles:gestionar'),('ADMIN','auditoria:ver'),
  ('SUPERVISOR','dashboard:ver'),('SUPERVISOR','incidentes:ver'),('SUPERVISOR','incidentes:crear'),('SUPERVISOR','incidentes:editar'),
  ('SUPERVISOR','revision:aprobar'),('SUPERVISOR','monitoreo:ver'),('SUPERVISOR','reportes:ver'),
  ('ANALISTA','dashboard:ver'),('ANALISTA','incidentes:ver'),('ANALISTA','incidentes:crear'),('ANALISTA','incidentes:editar'),
  ('AUDITOR','dashboard:ver'),('AUDITOR','incidentes:ver'),('AUDITOR','reportes:ver'),('AUDITOR','monitoreo:ver'),('AUDITOR','auditoria:ver');
