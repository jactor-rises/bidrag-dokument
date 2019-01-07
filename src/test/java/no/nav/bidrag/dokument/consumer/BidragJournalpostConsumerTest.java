package no.nav.bidrag.dokument.consumer;

import static no.nav.bidrag.dokument.BidragDokumentTest.bearer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommandDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.NyJournalpostCommandDto;

@DisplayName("BidragJournalpostConsumer")
@SuppressWarnings("unchecked")
class BidragJournalpostConsumerTest {

    private BidragJournalpostConsumer bidragJournalpostConsumer;

    private @Mock Appender appenderMock;
    private @Mock RestTemplate restTemplateMock;

    @BeforeEach
    void setup() {
        initMocks();
        initTestClass();
        mockRestTemplateFactory();
        mockLogAppender();
    }

    private void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    private void initTestClass() {
        bidragJournalpostConsumer = new BidragJournalpostConsumer("http://bidrag-dokument.nav.no");
    }

    private void mockRestTemplateFactory() {
        RestTemplateFactory.use(() -> restTemplateMock);
    }

    private void mockLogAppender() {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        when(appenderMock.getName()).thenReturn("MOCK");
        when(appenderMock.isStarted()).thenReturn(true);
        logger.addAppender(appenderMock);
    }

    @AfterEach
    void shouldResetRestTemplateFactory() {
        RestTemplateFactory.reset();
    }

    @DisplayName("skal bruke bidragssakens saksnummer i sti til tjeneste")
    @Test
    void shouldUseValueFromPath() {
        when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any())).thenReturn(
                new ResponseEntity<>(HttpStatus.NO_CONTENT));

        bidragJournalpostConsumer.finnJournalposter("101", "BID", bearer());
        verify(restTemplateMock).exchange(eq("/sak/101?fagomrade=BID"), eq(HttpMethod.GET), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any());
    }

    @DisplayName("should log get invocations")
    @Test
    void shouldLogGetInvocations() {
        when(restTemplateMock.exchange(anyString(), any(), any(), (ParameterizedTypeReference<List<JournalpostDto>>) any())).thenReturn(
                new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        bidragJournalpostConsumer.finnJournalposter("101", "FAR", bearer());

        verify(appenderMock).doAppend(
                argThat((ArgumentMatcher) argument -> {
                    assertThat(((ILoggingEvent) argument).getFormattedMessage())
                            .contains("Fikk http status 500 INTERNAL_SERVER_ERROR fra journalposter i bidragssak med saksnummer 101 på fagområde FAR");

                    return true;
                }));
    }

    @DisplayName("should log get invocations for single entity")
    @Test
    void shouldLogGetInvocationsForSingleEntity() {

        when(restTemplateMock.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                ArgumentMatchers.<Class<JournalpostDto>> any()))
                        .thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        bidragJournalpostConsumer.hentJournalpost(101, bearer());

        verify(appenderMock).doAppend(
                argThat((ArgumentMatcher) argument -> {
                    assertThat(((ILoggingEvent) argument).getFormattedMessage())
                            .isEqualTo("JournalpostDto med id=101 har http status 500 INTERNAL_SERVER_ERROR");

                    return true;
                }));
    }

    @DisplayName("should log new commands")
    @Test
    void shouldLogNewCommands() {
        when(restTemplateMock.exchange(anyString(), eq(HttpMethod.POST), any(), eq(JournalpostDto.class))).thenReturn(
                new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        bidragJournalpostConsumer.registrer(new NyJournalpostCommandDto(), bearer());

        verify(appenderMock).doAppend(
                argThat((ArgumentMatcher) argument -> {
                    assertThat(((ILoggingEvent) argument).getFormattedMessage())
                            .contains("Fikk http status 500 INTERNAL_SERVER_ERROR fra registrer ny journalpost: NyJournalpostCommandDto");

                    return true;
                }));
    }

    @DisplayName("should log edit commands")
    @Test
    void shouldLogEditCommands() {
        when(restTemplateMock.exchange(anyString(), any(), any(), eq(JournalpostDto.class))).thenReturn(
                new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        bidragJournalpostConsumer.endre(new EndreJournalpostCommandDto(), bearer());

        verify(appenderMock).doAppend(
                argThat((ArgumentMatcher) argument -> {
                    assertThat(((ILoggingEvent) argument).getFormattedMessage())
                            .contains("Fikk http status 500 INTERNAL_SERVER_ERROR fra endre journalpost: EndreJournalpostCommandDto");

                    return true;
                }));
    }
}
