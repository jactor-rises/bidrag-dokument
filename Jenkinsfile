
// Pipeline parametere må defineres før shared lib kjører
def envout = EnvironmentOut

@Library('bidrag-dokument-jenkins') _
   bidragDokumentPipeline {
      mvnImage = "maven:3.6.0-jdk-11-slim"
      application = "bidrag-dokument"
      branch = "master"
      environment = envout
   }
   
