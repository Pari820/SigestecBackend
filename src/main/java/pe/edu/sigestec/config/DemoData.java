package pe.edu.sigestec.config;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.sigestec.entity.auth.Perfil;
import pe.edu.sigestec.entity.auth.Rol;
import pe.edu.sigestec.entity.auth.Usuario;
import pe.edu.sigestec.entity.auth.UsuarioRol;
import pe.edu.sigestec.entity.catalogos.PersonaJuridica;
import pe.edu.sigestec.entity.catalogos.PersonaNatural;
import pe.edu.sigestec.entity.catalogos.TipoServicio;
import pe.edu.sigestec.entity.personal.Cargo;
import pe.edu.sigestec.entity.personal.Empleado;
import pe.edu.sigestec.entity.personal.Tecnico;
import pe.edu.sigestec.repository.auth.PerfilRepository;
import pe.edu.sigestec.repository.auth.RolRepository;
import pe.edu.sigestec.repository.auth.UsuarioRepository;
import pe.edu.sigestec.repository.auth.UsuarioRolRepository;
import pe.edu.sigestec.repository.catalogos.PersonaJuridicaRepository;
import pe.edu.sigestec.repository.catalogos.PersonaNaturalRepository;
import pe.edu.sigestec.repository.catalogos.TipoServicioRepository;
import pe.edu.sigestec.repository.personal.CargoRepository;
import pe.edu.sigestec.repository.personal.EmpleadoRepository;
import pe.edu.sigestec.repository.personal.TecnicoRepository;

@Configuration
public class DemoData {
    @Bean
    CommandLineRunner seedDemo(DemoSeed s, @Value("${app.demo.enabled:false}") boolean demo) {
        return args -> {
            if (demo) s.seed();
        };
    }

    @org.springframework.stereotype.Service
    public static class DemoSeed {
        private final PerfilRepository perfiles;
        private final PersonaNaturalRepository naturales;
        private final PersonaJuridicaRepository empresas;
        private final CargoRepository cargos;
        private final EmpleadoRepository empleados;
        private final TecnicoRepository tecnicos;
        private final UsuarioRepository usuarios;
        private final RolRepository roles;
        private final UsuarioRolRepository usuarioRoles;
        private final TipoServicioRepository tipos;
        private final PasswordEncoder encoder;
        @Value("${app.demo.password:Cambiar2026!}")
        private String claveDemo;

        public DemoSeed(PerfilRepository perfiles, PersonaNaturalRepository naturales, PersonaJuridicaRepository empresas,
                        CargoRepository cargos, EmpleadoRepository empleados, TecnicoRepository tecnicos, UsuarioRepository usuarios,
                        RolRepository roles, UsuarioRolRepository usuarioRoles, TipoServicioRepository tipos, PasswordEncoder encoder) {
            this.perfiles = perfiles;
            this.naturales = naturales;
            this.empresas = empresas;
            this.cargos = cargos;
            this.empleados = empleados;
            this.tecnicos = tecnicos;
            this.usuarios = usuarios;
            this.roles = roles;
            this.usuarioRoles = usuarioRoles;
            this.tipos = tipos;
            this.encoder = encoder;
        }

        @Transactional
        public void seed() {
            if (usuarios.count() > 0) { asegurarAlmacenero(); return; }
            for (String rol : new String[]{"ADMIN", "GERENTE", "SUPERVISOR", "SSOMA", "ALMACENERO", "TECNICO", "APOYO"}) {
                Cargo c = new Cargo();
                c.setNombre(rol);
                cargos.save(c);
                if (!"TECNICO".equals(rol) && !"APOYO".equals(rol)) {
                    Rol r = new Rol();
                    r.setNombre(rol);
                    roles.save(r);
                }
            }
            for (String[] p : new String[][]{{"admin", "ADMIN"}, {"gerente", "GERENTE"}, {"supervisor", "SUPERVISOR"}, {"ssoma", "SSOMA"}, {"almacenero", "ALMACENERO"}}) {
                Perfil pf = new Perfil();
                pf.setTipoPerfil("NATURAL");
                perfiles.save(pf);
                PersonaNatural nat = new PersonaNatural();
                nat.setPerfil(pf);
                nat.setNombres("Usuario");
                nat.setApellidos(p[0]);
                naturales.save(nat);
                Empleado emp = new Empleado();
                emp.setPersonaNatural(nat);
                emp.setCargo(cargos.findByNombreIgnoreCase(p[1]).orElseThrow());
                emp.setCodigoEmpleado("EMP-" + p[0].toUpperCase());
                emp.setFechaIngreso(LocalDate.now());
                empleados.save(emp);
                Usuario u = new Usuario();
                u.setPersonaNatural(nat);
                u.setNombreUsuario(p[0]);
                u.setClaveHash(encoder.encode(claveDemo));
                usuarios.save(u);
                UsuarioRol ur = new UsuarioRol();
                ur.setUsuario(u);
                ur.setRol(roles.findByNombre(p[1]).orElseThrow());
                usuarioRoles.save(ur);
            }
            for (String[] p : new String[][]{{"Carlos", "TECNICO"}, {"Pedro", "TECNICO"}, {"María", "APOYO"}}) {
                Perfil pf = new Perfil();
                pf.setTipoPerfil("NATURAL");
                perfiles.save(pf);
                PersonaNatural nat = new PersonaNatural();
                nat.setPerfil(pf);
                nat.setNombres(p[0]);
                nat.setApellidos("Demostración");
                naturales.save(nat);
                Empleado e = new Empleado();
                e.setPersonaNatural(nat);
                e.setCargo(cargos.findByNombreIgnoreCase(p[1]).orElseThrow());
                e.setCodigoEmpleado("EMP-" + p[0].toUpperCase());
                empleados.saveAndFlush(e);
                if ("TECNICO".equals(p[1])) {
                    Tecnico te = new Tecnico();
                    te.setEmpleado(e);
                    tecnicos.save(te);
                }
            }
            Perfil pf = new Perfil();
            pf.setTipoPerfil("JURIDICA");
            perfiles.save(pf);
            PersonaJuridica pj = new PersonaJuridica();
            pj.setPerfil(pf);
            pj.setRuc("20123456789");
            pj.setRazonSocial("Cliente de demostración");
            empresas.save(pj);
            for (String tipo : new String[]{"Mantenimiento eléctrico", "Inspección técnica"}) {
                TipoServicio t = new TipoServicio();
                t.setNombre(tipo);
                tipos.save(t);
            }
        }


        private void asegurarAlmacenero() {
            if (usuarios.findByNombreUsuario("almacenero").isPresent()) return;
            Cargo cargo = cargos.findByNombreIgnoreCase("ALMACENERO").orElseGet(() -> {
                Cargo c = new Cargo(); c.setNombre("ALMACENERO"); return cargos.save(c);
            });
            Rol rol = roles.findByNombre("ALMACENERO").orElseGet(() -> {
                Rol r = new Rol(); r.setNombre("ALMACENERO"); return roles.save(r);
            });
            Perfil perfil = new Perfil(); perfil.setTipoPerfil("NATURAL"); perfiles.save(perfil);
            PersonaNatural natural = new PersonaNatural(); natural.setPerfil(perfil);
            natural.setNombres("Usuario"); natural.setApellidos("almacenero"); naturales.save(natural);
            Empleado empleado = new Empleado(); empleado.setPersonaNatural(natural);
            empleado.setCargo(cargo); empleado.setCodigoEmpleado("EMP-ALMACENERO"); empleados.save(empleado);
            Usuario usuario = new Usuario(); usuario.setPersonaNatural(natural);
            usuario.setNombreUsuario("almacenero"); usuario.setClaveHash(encoder.encode(claveDemo)); usuarios.save(usuario);
            UsuarioRol ur = new UsuarioRol(); ur.setUsuario(usuario); ur.setRol(rol); usuarioRoles.save(ur);
        }
    }
}
