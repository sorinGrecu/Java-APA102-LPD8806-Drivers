package com.sorin.grecu;

import com.sorin.grecu.ledstrip.ledStrip.LedStrip;
import com.sorin.grecu.ledstrip.ledStrip.NetworkUtils;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

@SpringBootApplication
public class LedstripApplication implements CommandLineRunner {

    @Autowired
    @Qualifier("${ledStrip.name}")
    LedStrip ledStrip;

    private static boolean headless = NetworkUtils.isMachineRpi() ? true : false;

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(LedstripApplication.class);
        builder.headless(headless).run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (!headless) {
            ledStrip.displayGraphics();
        }
    }
    static {
        System.setProperty("pi4j.linking", "dynamic");
    }

    @Component
    public static class AutowireCandidateResolverConfigurer implements BeanFactoryPostProcessor {
        private static class EnvironmentAwareQualifierAnnotationAutowireCandidateResolver extends QualifierAnnotationAutowireCandidateResolver {
            private static class ResolvedQualifier implements Qualifier {
                private final String value;
                ResolvedQualifier(String value) {
                    this.value = value;
                }
                @Override
                public String value() {
                    return this.value;
                }
                @Override
                public Class<? extends Annotation> annotationType() {
                    return Qualifier.class;
                }
            }

            @Override
            protected boolean checkQualifier(BeanDefinitionHolder bdHolder, Annotation annotation, TypeConverter typeConverter) {
                if (annotation instanceof Qualifier) {
                    Qualifier qualifier = (Qualifier) annotation;
                    if (qualifier.value().startsWith("${") && qualifier.value().endsWith("}")) {
                        DefaultListableBeanFactory bf = (DefaultListableBeanFactory) this.getBeanFactory();
                        ResolvedQualifier resolvedQualifier = new ResolvedQualifier(bf.resolveEmbeddedValue(qualifier.value()));
                        return super.checkQualifier(bdHolder, resolvedQualifier, typeConverter);
                    }
                }
                return super.checkQualifier(bdHolder, annotation, typeConverter);
            }
        }
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
            DefaultListableBeanFactory bf = (DefaultListableBeanFactory) beanFactory;
            bf.setAutowireCandidateResolver(new EnvironmentAwareQualifierAnnotationAutowireCandidateResolver());
        }
    }
}
