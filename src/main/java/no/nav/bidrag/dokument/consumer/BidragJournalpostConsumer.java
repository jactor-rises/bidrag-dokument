package no.nav.bidrag.dokument.consumer;

import static no.nav.bidrag.dokument.BidragDokumentConfig.ISSUER;
import static no.nav.bidrag.dokument.consumer.ConsumerUtil.addSecurityHeader;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommandDto;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.NyJournalpostCommandDto;
import no.nav.security.oidc.context.OIDCRequestContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class BidragJournalpostConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(BidragJournalpostConsumer.class);
  private static final String PATH_JOURNALPOST = "/journalpost";
  private static final String PATH_SAK = "/sak/";
  private static final String PARAM_FAGOMRADE = "fagomrade";

  private final OIDCRequestContextHolder securityContextHolder;
  private final RestTemplate restTemplate;

  public BidragJournalpostConsumer(OIDCRequestContextHolder securityContextHolder, RestTemplate restTemplate) {
    this.securityContextHolder = securityContextHolder;
    this.restTemplate = restTemplate;
  }

  public List<JournalpostDto> finnJournalposter(String saksnummer, String fagomrade) {
    String path = PATH_SAK + saksnummer;

    String uri = UriComponentsBuilder.fromPath(path)
        .queryParam(PARAM_FAGOMRADE, fagomrade)
        .toUriString();

    ResponseEntity<List<JournalpostDto>> journalposterForBidragRequest = restTemplate.exchange(
        uri, HttpMethod.GET, addSecurityHeader(null, getBearerToken()), typereferansenErListeMedJournalposter()
    );

    HttpStatus httpStatus = journalposterForBidragRequest.getStatusCode();
    LOGGER.info("Fikk http status {} fra journalposter i bidragssak med saksnummer {} på fagområde {}", httpStatus, saksnummer, fagomrade);
    List<JournalpostDto> journalposter = journalposterForBidragRequest.getBody();

    return journalposter != null ? journalposter : Collections.emptyList();
  }

  private static ParameterizedTypeReference<List<JournalpostDto>> typereferansenErListeMedJournalposter() {
    return new ParameterizedTypeReference<>() {
    };
  }

  public Optional<JournalpostDto> registrer(NyJournalpostCommandDto nyJournalpostCommandDto) {
    String path = PATH_JOURNALPOST + "/ny";

    ResponseEntity<JournalpostDto> registrertJournalpost = restTemplate.exchange(
        path, HttpMethod.POST, addSecurityHeader(new HttpEntity<>(nyJournalpostCommandDto), getBearerToken()), JournalpostDto.class
    );

    HttpStatus httpStatus = registrertJournalpost.getStatusCode();
    LOGGER.info("Fikk http status {} fra registrer ny journalpost: {}", httpStatus, nyJournalpostCommandDto);

    return Optional.ofNullable(registrertJournalpost.getBody());
  }

  public Optional<JournalpostDto> hentJournalpost(Integer id) {
    String path = PATH_JOURNALPOST + '/' + id;

    ResponseEntity<JournalpostDto> journalpostResponseEntity = restTemplate.exchange(
        path, HttpMethod.GET, addSecurityHeader(null, getBearerToken()), JournalpostDto.class
    );

    HttpStatus httpStatus = journalpostResponseEntity.getStatusCode();

    LOGGER.info("JournalpostDto med id={} har http status {}", id, httpStatus);

    return Optional.ofNullable(journalpostResponseEntity.getBody());
  }

  public Optional<JournalpostDto> endre(EndreJournalpostCommandDto endreJournalpostCommandDto) {
    ResponseEntity<JournalpostDto> endretJournalpost = restTemplate.exchange(
        PATH_JOURNALPOST, HttpMethod.POST, addSecurityHeader(new HttpEntity<>(endreJournalpostCommandDto), getBearerToken()), JournalpostDto.class
    );

    HttpStatus httpStatus = endretJournalpost.getStatusCode();
    LOGGER.info("Fikk http status {} fra endre journalpost: {}", httpStatus, endreJournalpostCommandDto);

    return Optional.ofNullable(endretJournalpost.getBody());
  }

  private String getBearerToken() {
    return "Bearer " + securityContextHolder.getOIDCValidationContext().getToken(ISSUER).getIdToken();
  }
}
