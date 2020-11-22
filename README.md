# Applikasjonen er bygget i Maven og skrevt i Kotlin.
<!--- Travis CI build status banner -->
[![Build Status](https://travis-ci.com/Opkris/devops-eksamen.svg?branch=master)](https://travis-ci.com/github/Opkris/devops-eksamen)

#Oppsett

###kryptering av hemligheter i Travis:

#####Lag en Service Account for Travis i Gcloud.

* Gi service acounten Rollene: 
    - _Google Storage Admin._
    - _Container Reistry Service Agent._ 
    - _Cloud Run Service Agent._
 
* Last med nøkkelfil for service account(en) og denne skal legges under/i root filen i prosjektet og 
    gi filen navnet:
     
          
    GCP-key.json
    
* Kryptering av filer og andre hemmeligheter
    - `NB! Detter krevet at du er logget på Travis-CI` 
    - Windows OS: finn prosjektet i "terminalen" skriv: 
    
    
    docker run -v $(pwd): --rm skandyla/travis-cli encrypt-file GCP-key.json --add 
    
    
(for noen kan dette være problematisk, slik det var for meg, anbefaler et annet OS eg. Linux i VM)
 
   - Mac/Linux ("Unix") OS: der kan man skrive i terminalen:
   
   
    travis encrypt-file --pro GCP-key.json --add
    
* Andre hemmeligheter:

    
    travis encrypt --pro LOGZ_TOKEN=<Logz.io token til bruker> --add
    travis encrypt --pro LOGZ_URL=<Logz.io URL for Logz.io> --add
    
    
Verdiene for `LOGZ_TOKEN` OG `LOGZ_URL` kan du finne inne på: 
`Logz.io` -> `Send your data` -> `Libraries` -> `Java - logback appender`

      
* Hvis alt har blitt gjort ordentlig skal projektet vare konfigurert med riktige hemmeligeter,
    Da trenger man kun å gjøre en push til master branch. og Travis vil lage et Docker Image for deg.  
    
    