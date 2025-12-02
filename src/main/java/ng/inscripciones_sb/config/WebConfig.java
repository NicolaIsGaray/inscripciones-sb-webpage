package ng.inscripciones_sb.config;

import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. Manejo del index.html: Forzar revalidación o No-Cache
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.noCache()
                        .mustRevalidate()); // O usar noStore()

        // 2. Manejo de archivos hasheados por Angular: Caché agresiva
        // (Angular ya asegura que el nombre cambia si el contenido lo hace)
        registry.addResourceHandler("/*.js", "/*.css")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS)
                        .cachePublic());

        // 3. Manejo de assets no hasheados: Menos agresivo que los hasheados
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES));
    }
}
