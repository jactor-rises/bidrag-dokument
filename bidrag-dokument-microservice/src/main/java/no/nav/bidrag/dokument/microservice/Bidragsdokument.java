package no.nav.bidrag.dokument.microservice;

import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.consumer.JournalforingConsumer;
import no.nav.bidrag.dokument.service.JournalpostMapper;
import no.nav.bidrag.dokument.service.JournalpostService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@SpringBootApplication
@PropertySource("classpath:url.properties")
public class Bidragsdokument extends WebMvcConfigurationSupport {

    @Bean public JournalpostService journalpostService(BidragJournalpostConsumer bidragJournalpostConsumer, JournalforingConsumer journalpostConsumer) {
        return new JournalpostService(bidragJournalpostConsumer, journalpostConsumer, new JournalpostMapper());
    }

    @Bean public BidragJournalpostConsumer bidragJournalpostConsumer(
            @Value("${JOURNALPOST_URL}") String bidragBaseUrl
    ) {
        return new BidragJournalpostConsumer(bidragBaseUrl + "/bidrag-journalpost/sak/");
    }

    @Bean public JournalforingConsumer journalforingConsumer(
            @Value("${JOARK_URL}") String joarkRestServiceUrl
    ) {
        return new JournalforingConsumer(joarkRestServiceUrl);
    }

    public static void main(String[] args) {
        SpringApplication.run(Bidragsdokument.class, args);
    }
}
