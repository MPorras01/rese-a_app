package com.resenias.reviews.config;

import java.util.List;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.resenias.reviews.entity.Business;
import com.resenias.reviews.entity.Business.BusinessStatus;
import com.resenias.reviews.entity.Product;
import com.resenias.reviews.entity.Review;
import com.resenias.reviews.entity.User;
import com.resenias.reviews.repository.BusinessRepository;
import com.resenias.reviews.repository.ProductRepository;
import com.resenias.reviews.repository.ReviewRepository;
import com.resenias.reviews.repository.UserRepository;

@Configuration
@Profile({ "dev", "docker" })
public class DevSampleDataConfig {

    @Bean
    public ApplicationRunner seedSampleData(UserRepository userRepo,
                                            BusinessRepository businessRepo,
                                            ProductRepository productRepo,
                                            ReviewRepository reviewRepo,
                                            PasswordEncoder encoder) {
        return args -> {
            // Solo seeda si no hay negocios aprobados todavía
            if (businessRepo.countByStatus(BusinessStatus.APPROVED) > 0) {
                return;
            }

            // ── Usuarios ──────────────────────────────────────────────────────
            User owner1 = saveUser(userRepo, encoder,
                 "dueno1@resena.local", "Carlos Propietario", "Owner12345!", User.Role.USER);
            User owner2 = saveUser(userRepo, encoder,
                 "dueno2@resena.local", "Maria Empresaria", "Owner12345!", User.Role.USER);
            User cli1 = saveUser(userRepo, encoder,
                "cliente1@resena.local", "Ana Garcia", "Client12345!", User.Role.USER);
            User cli2 = saveUser(userRepo, encoder,
                "cliente2@resena.local", "Pedro Lopez", "Client12345!", User.Role.USER);
            User cli3 = saveUser(userRepo, encoder,
                "cliente3@resena.local", "Sofia Martinez", "Client12345!", User.Role.USER);

            // ── Negocios APROBADOS ────────────────────────────────────────────
            Business terraza = businessRepo.save(Business.builder()
                .owner(owner1).name("La Terraza Gourmet")
                .description("Restaurante de cocina fusion colombiana con vista panoramica al parque. " +
                    "Especialidad en carnes a la brasa, mariscos frescos y cocteles artesanales. " +
                    "Ambiente acogedor para celebraciones y reuniones de negocios.")
                .category("Restaurante").address("Carrera 7 #45-12").city("Bogota")
                .phone("+573151001001").email("terraza@resena.local")
                .website("https://laterraza.example.com")
                .status(BusinessStatus.APPROVED).build());

            Business tiendaDigital = businessRepo.save(Business.builder()
                .owner(owner1).name("Tienda Digital XYZ")
                .description("Tecnologia, accesorios y gadgets al mejor precio. " +
                    "Distribuidor oficial de las principales marcas del mercado. " +
                    "Envios a todo el pais y soporte tecnico post-venta.")
                .category("Tienda").address("Centro Comercial El Tesoro Local 204").city("Medellin")
                .phone("+573151002002").email("tienda@resena.local")
                .website("https://tiendaxyz.example.com")
                .status(BusinessStatus.APPROVED).build());

            Business centroMedico = businessRepo.save(Business.builder()
                .owner(owner2).name("Centro Medico Vida Plena")
                .description("Clinica ambulatoria con servicios de medicina general, odontologia y nutricion. " +
                    "Atencion personalizada con citas online de 7am a 8pm de lunes a sabado. " +
                    "Laboratorios clinicos propios con resultados en 24 horas.")
                .category("Salud").address("Avenida 6N #28-15").city("Cali")
                .phone("+573151003003").email("medico@resena.local")
                .status(BusinessStatus.APPROVED).build());

            Business salonBelleza = businessRepo.save(Business.builder()
                .owner(owner2).name("Salon Belleza Iris")
                .description("Estudio especializado en colorimetria, cortes modernos y tratamientos capilares. " +
                    "Equipo de estilistas certificados en las mejores academias internacionales. " +
                    "Productos premium libres de sulfatos y parabenos.")
                .category("Belleza").address("Calle 85 #11-64").city("Bogota")
                .phone("+573151004004").email("iris@resena.local")
                .status(BusinessStatus.APPROVED).build());

            Business academia = businessRepo.save(Business.builder()
                .owner(owner1).name("Academia Tech Skills")
                .description("Cursos intensivos de programacion, diseño UX y marketing digital. " +
                    "Mentores con experiencia en empresas de Silicon Valley. " +
                    "Modalidad presencial y virtual con certificados reconocidos por la industria.")
                .category("Educacion").address("Carrera 15 #93-47 Piso 3").city("Bogota")
                .phone("+573151005005").email("academia@resena.local")
                .website("https://techskills.example.com")
                .status(BusinessStatus.APPROVED).build());

            // ── Negocios PENDIENTES (para probar panel Admin) ─────────────────
            businessRepo.save(Business.builder()
                .owner(owner2).name("Panaderia El Trigo Dorado")
                .description("Panaderia artesanal con recetas tradicionales desde 1985. " +
                    "Panes de masa madre, croissants y pasteles elaborados cada manana.")
                .category("Restaurante").address("Calle 10 #2-34").city("Manizales")
                .phone("+573151006006").email("trigo@resena.local")
                .status(BusinessStatus.PENDING).build());

            businessRepo.save(Business.builder()
                .owner(owner1).name("Gym PowerFit Centro")
                .description("Gimnasio equipado con maquinas de ultima generacion. " +
                    "Clases grupales de spinning, yoga y funcional incluidas.")
                .category("Servicio").address("Avenida El Dorado #65-23").city("Bogota")
                .phone("+573151007007").email("powerfit@resena.local")
                .status(BusinessStatus.PENDING).build());

            // ── Negocio RECHAZADO ────────────────────────────────────────────
            businessRepo.save(Business.builder()
                .owner(owner2).name("Empresa Fantasma SAS")
                .description("Descripcion incompleta.")
                .category("Otro").city("Bogota")
                .status(BusinessStatus.REJECTED)
                .rejectionReason("Informacion insuficiente y datos de contacto invalidos.")
                .build());

            // ── Productos ────────────────────────────────────────────────────
            saveProducts(productRepo, terraza, List.of(
                new String[]{"Lomo al Carbon", "Filete de res 300g con chimichurri y papa criolla", "$65,000"},
                new String[]{"Ceviche de Camaron", "Camaron fresco marinado en limon con aguacate", "$48,000"},
                new String[]{"Bandeja Paisa", "Plato tipico completo con frijoles, chicharron y arepa", "$38,000"},
                new String[]{"Coctel de la Casa", "Mezcla artesanal de frutas tropicales con aguardiente", "$22,000"}
            ));

            saveProducts(productRepo, tiendaDigital, List.of(
                new String[]{"Audifonos Bluetooth Pro", "Cancelacion de ruido activa, 30h bateria", "$280,000"},
                new String[]{"Cable USB-C Reforzado", "2m trenzado en nylon, carga rapida 65W", "$35,000"},
                new String[]{"Soporte Laptop Ergonomico", "Aluminio ajustable en 6 alturas", "$120,000"}
            ));

            saveProducts(productRepo, centroMedico, List.of(
                new String[]{"Consulta Medicina General", "Atencion medica completa con historia clinica", "$80,000"},
                new String[]{"Limpieza Dental Profesional", "Profilaxis con ultrasonido incluye revision", "$95,000"},
                new String[]{"Perfil Lipidico Completo", "Colesterol HDL, LDL, trigliceridos y glucosa", "$55,000"}
            ));

            saveProducts(productRepo, salonBelleza, List.of(
                new String[]{"Coloracion Completa", "Tinte profesional con tratamiento hidratante", "$180,000"},
                new String[]{"Corte + Blow Dry", "Corte personalizado y secado con brushing", "$85,000"}
            ));

            saveProducts(productRepo, academia, List.of(
                new String[]{"Bootcamp Full Stack JS", "12 semanas intensivas React + Node.js", "$3,500,000"},
                new String[]{"Curso UX/UI Design", "8 semanas Figma, investigacion y prototipado", "$2,200,000"},
                new String[]{"Marketing Digital 360", "Google Ads, Meta Ads y SEO en 6 semanas", "$1,800,000"}
            ));

            // ── Reseñas con ratings variados ─────────────────────────────────
            // La Terraza (avg esperado ~4.2)
            review(reviewRepo, cli1, terraza, 5,
                "Increible experiencia, el lomo al carbon es de otro nivel. El servicio fue impecable y la vista al parque excelente. Definitivamente vuelvo.");
            review(reviewRepo, cli2, terraza, 4,
                "Buena comida aunque un poco cara. El ceviche fresco y sabroso. El ambiente es agradable pero el servicio tardo un poco en hora pico.");
            review(reviewRepo, cli3, terraza, 5,
                "El mejor restaurante de la zona sin duda. La bandeja paisa es generosa y autentica. Recomendado 100% para celebraciones");
            review(reviewRepo, cli1, terraza, 3,
                "La comida bien pero el tiempo de espera fue de 40 minutos. Esperaba mas por los precios que manejan.");
            review(reviewRepo, cli2, terraza, 4,
                "Muy buen lugar. Los cocteles artesanales son deliciosos. Atmosfera ideal para una cita especial.");

            // Tienda Digital (avg esperado ~3.4)
            review(reviewRepo, cli3, tiendaDigital, 5,
                "Los audifonos son excelentes! Calidad de sonido impresionante y la cancelacion de ruido funciona perfecto en el metro.");
            review(reviewRepo, cli1, tiendaDigital, 4,
                "Buen servicio y variedad de productos. El cable USB-C es resistente, ya llevo 3 meses usandolo sin problemas.");
            review(reviewRepo, cli2, tiendaDigital, 2,
                "Me vendieron un soporte que no era compatible con mi laptop, tuve que devolverlo. La devolucion fue lenta y complicada.");
            review(reviewRepo, cli3, tiendaDigital, 3,
                "Precios razonables pero el local es pequeno y hay poco stock. Tuve que esperar semana y media por un accesorio.");

            // Centro Medico (avg esperado ~4.7)
            review(reviewRepo, cli1, centroMedico, 5,
                "Atencion medica excelente. El doctor muy profesional y dedicado. Los resultados del laboratorio llegaron en menos de 24 horas.");
            review(reviewRepo, cli2, centroMedico, 5,
                "La limpieza dental fue sin dolor y muy completa. La odontologa explico todo el procedimiento paso a paso. Regresare cada 6 meses.");
            review(reviewRepo, cli3, centroMedico, 4,
                "Buena clinica, limpia y organizada. El unico inconveniente es que el parqueadero es limitado. El personal de recepcion muy amable.");

            // Salon Belleza (avg esperado ~4.0)
            review(reviewRepo, cli2, salonBelleza, 5,
                "La coloracion quedo espectacular! Exactamente el tono que queria. La estilista muy profesional y el tratamiento hidratante noto la diferencia.");
            review(reviewRepo, cli3, salonBelleza, 3,
                "El corte bien pero tuve que esperar 45 minutos extra sin aviso. Espero que mejoren la gestion de citas.");

            // Academia Tech Skills (avg esperado ~3.0)
            review(reviewRepo, cli1, academia, 4,
                "El bootcamp full stack intenso pero muy completo. Los mentores con mucha experiencia real. Consegui trabajo al mes de terminar.");
            review(reviewRepo, cli3, academia, 2,
                "El curso de marketing no cumplio las expectativas. El contenido desactualizado y el soporte muy lento para responder dudas.");
        };
    }

    private User saveUser(UserRepository repo, PasswordEncoder enc,
                          String email, String name, String pass, User.Role role) {
        return repo.findByEmail(email).orElseGet(() -> repo.save(User.builder()
            .email(email).name(name)
            .passwordHash(enc.encode(pass))
            .emailVerified(true).phoneVerified(true)
            .status(User.UserStatus.ACTIVE)
            .oauthProvider(User.OAuthProvider.LOCAL)
            .role(role)
            .build()));
    }

    private void saveProducts(ProductRepository repo, Business b, List<String[]> items) {
        for (String[] item : items) {
            repo.save(Product.builder()
                .business(b).name(item[0]).description(item[1])
                .priceRange(item.length > 2 ? item[2] : null)
                .active(true).build());
        }
    }

    private void review(ReviewRepository repo, User u, Business b, int rating, String body) {
        repo.save(Review.builder().user(u).business(b)
            .rating((short) rating).body(body).build());
    }
}
