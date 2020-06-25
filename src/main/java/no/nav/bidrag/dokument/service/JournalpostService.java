package no.nav.bidrag.dokument.service;

import static no.nav.bidrag.commons.KildesystemIdenfikator.Kildesystem.BIDRAG;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import no.nav.bidrag.commons.KildesystemIdenfikator;
import no.nav.bidrag.commons.web.HttpStatusResponse;
import no.nav.bidrag.dokument.consumer.BidragArkivConsumer;
import no.nav.bidrag.dokument.consumer.BidragJournalpostConsumer;
import no.nav.bidrag.dokument.dto.AvvikType;
import no.nav.bidrag.dokument.dto.Avvikshendelse;
import no.nav.bidrag.dokument.dto.EndreJournalpostCommand;
import no.nav.bidrag.dokument.dto.JournalpostDto;
import no.nav.bidrag.dokument.dto.JournalpostResponse;
import no.nav.bidrag.dokument.dto.OpprettAvvikshendelseResponse;
import no.nav.bidrag.dokument.dto.RegistrereJournalpostCommand;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class JournalpostService {

  private final BidragJournalpostConsumer bidragJournalpostConsumer;
  private final BidragArkivConsumer bidragArkivConsumer;

  public JournalpostService(
      BidragArkivConsumer bidragArkivConsumer,
      BidragJournalpostConsumer bidragJournalpostConsumer
  ) {
    this.bidragArkivConsumer = bidragArkivConsumer;
    this.bidragJournalpostConsumer = bidragJournalpostConsumer;
  }

  public HttpStatusResponse<JournalpostResponse> hentJournalpost(String saksnummer, KildesystemIdenfikator kildesystemIdenfikator) {
    if (kildesystemIdenfikator.erFor(BIDRAG)) {
      return bidragJournalpostConsumer.hentJournalpostResponse(saksnummer, kildesystemIdenfikator.getPrefiksetJournalpostId());
    }

    return bidragArkivConsumer.hentJournalpost(saksnummer, kildesystemIdenfikator.getPrefiksetJournalpostId());
  }

  public HttpStatusResponse<List<AvvikType>> finnAvvik(String saksnummer, KildesystemIdenfikator kildesystemIdenfikator) {
    if (kildesystemIdenfikator.erFor(BIDRAG)) {
      return bidragJournalpostConsumer.finnAvvik(saksnummer, kildesystemIdenfikator.getPrefiksetJournalpostId());
    }

    return new HttpStatusResponse<>(HttpStatus.BAD_REQUEST, Collections.emptyList());
  }

  public HttpStatusResponse<OpprettAvvikshendelseResponse> opprettAvvik(
      String enhet, KildesystemIdenfikator kildesystemIdenfikator, Avvikshendelse avvikshendelse
  ) {
    if (kildesystemIdenfikator.erFor(BIDRAG)) {
      return bidragJournalpostConsumer.opprettAvvik(enhet, kildesystemIdenfikator.getPrefiksetJournalpostId(), avvikshendelse);
    }

    return new HttpStatusResponse<>(HttpStatus.BAD_REQUEST);
  }

  public List<JournalpostDto> finnJournalposter(String saksnummer, String fagomrade) {
    var sakjournal = new ArrayList<>(bidragJournalpostConsumer.finnJournalposter(saksnummer, fagomrade));
    sakjournal.addAll(bidragArkivConsumer.finnJournalposter(saksnummer, fagomrade));

    return sakjournal;
  }

  public HttpStatusResponse<Void> endre(String enhet, EndreJournalpostCommand endreJournalpostCommand) {
    return bidragJournalpostConsumer.endre(enhet, endreJournalpostCommand);
  }

  public void registrer(String enhet, RegistrereJournalpostCommand registrereJournalpostCommand) {
    bidragJournalpostConsumer.endre(enhet, map(registrereJournalpostCommand));
  }

  private EndreJournalpostCommand map(RegistrereJournalpostCommand registrereJournalpostCommand) {
    var endreJournalpostCommand = new EndreJournalpostCommand();
    endreJournalpostCommand.setJournalpostId(registrereJournalpostCommand.getJournalpostId());
    endreJournalpostCommand.setAvsenderNavn(registrereJournalpostCommand.getAvsenderNavn());
    endreJournalpostCommand.setBehandlingstema(registrereJournalpostCommand.getBehandlingstema());
    endreJournalpostCommand.setDokumentDato(registrereJournalpostCommand.getDokumentDato());
    endreJournalpostCommand.setEndreDokumenter(registrereJournalpostCommand.getEndreDokumenter());
    endreJournalpostCommand.setFagomrade(registrereJournalpostCommand.getFagomrade());
    endreJournalpostCommand.setGjelder(registrereJournalpostCommand.getGjelder());
    endreJournalpostCommand.setGjelderType(registrereJournalpostCommand.getGjelderType());
    endreJournalpostCommand.setJournalforendeEnhet(registrereJournalpostCommand.getJournalforendeEnhet());
    endreJournalpostCommand.setTilknyttSaker(registrereJournalpostCommand.getSaksnummer());
    endreJournalpostCommand.setTittel(registrereJournalpostCommand.getTittel());

    return endreJournalpostCommand;
  }
}
