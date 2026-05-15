# Sticker Safari

Kao selekcijski zadatak dobili ste izvorni kod aplikacije "Sticker Safari".

Aplikacija "Sticker Safari" napisana je u programskom jeziku Java i za razvoj je korištena najnovija verzija Spring Boota.

Radi se o aplikaciji za digitalne albume za skupljanje sličica. Korisnici mogu otvarati paketiće, gledati svoje popunjene albume i razmjenjivati se s drugim korisnicima. Tako bi barem trebala raditi aplikacija, no u stvarnosti nije sve bajno.  
Testovi padaju, greške se bacaju, backlog raste, a rokovi se bliže.

**Potrebne tehnologije**: Za rad na projektu potrebno je instalirati JDK 21 i Maven.

---

## "Implementirani" zahtjevi

### 1.
Svaki album ima barem jednu sličicu. Sličice sadrže edukativne informacije o životinji koju predstavljaju (ime, latinsko ime, stanište, opis, slika).  
Različiti albumi ne mogu sadržavati istu sličicu.

### 2.
Korisnike aplikacije obilježava korisničko ime i jedinstveni identifikator korisnika. Za svakog korisnika se prati i njihova kolekcija sličica u kojoj je moguće imati i duplikate.

### 3.
Korisnici mogu međusobno razmjenjivati sličice. Postoje dva načina razmjene sličica: globalni i osobni.  
Razmjena sličica obavlja se preko zahtjeva za razmjenu. Korisnik kojemu je ponuđen zahtjev može prihvatiti ili odbiti zahtjev za razmjenu.  
Kada korisnik zatraži globalnu razmjenu svi korisnici koji imaju traženu sličicu viška, a nemaju niti jednu ponuđenu dobiju zahtjev za razmjenu.  
Zahtjev za osobnom razmjenom šalje se korisniku koji je naveden pri kreiranju zahtjeva.  
Pri osobnoj razmjeni može se zatražiti bilo koja sličica koju korisnik ima u kolekciji.  
Dozvoljena je razmjena sličica iz različitih albuma.

### 4.
Korisnik treba imati mogućnost vidjeti listu razmjena koje su njemu poslane i koje je poslao drugim korisnicima, a još uvijek nisu prihvaćene ili odbijene.

### 5.
Korisnicima dolaze notifikacije. Notifikacija sadrži identifikator korisnika kojem je namijenjena, poruku i datum nastajanja.  
Notifikacije se šalju kad korisnik primi novi zahtjev za razmjenu ili korisnikov zahtjev bude odbijen/prihvaćen.  
Potrebno je i pratiti koje je notifikacije korisnik pročitao i omogućiti dohvaćanje samo ne pročitanih notifikacija.

### 6.
Korisnici za dobivanje novih sličica otvaraju paketiće. Pri otvaranju paketića korisnik navodi album i količinu sličica u paketiću kojeg otvara.  
Sve sličice unutar paketića imaju istu vjerojatnost da se otvore i moguće je otvoriti duplikate.

Npr. Korisnik XY otvara paketić albuma "Životinje i prijatelji" i upisuje da želi "kupiti" 5 sličica. Kao rezultat sustav mu daje sličice:  
1x br. 22, 2x br. 50, br.67 i br. 90.

---

Zadatak se sastoji od dva dijela.

Rješenje ne mora biti kompletno: ako poslano rješenje nema riješene sve elemente zadatka, ono neće biti odbijeno.

---

## 1. dio: Prepravljanje postojeće koda

Zadani kod nije primjer dobro napisane aplikacije, u njega smo ubacili loše programske prakse i razne bugove.

Vaš prvi zadatak je pronaći takve primjere u kodu i popraviti ih kako mislite da je najbolje.

Kao smjernice za početak rada dali smo vam testove koji padaju upravo zbog nekih od tih bugova.

Ali čak ni nakon što se svi ti testovi zazelene nije kraj posla.

Prođite kroz ostatak koda i popravite sve što vam se čini sumnjivo. Čak i ako nešto radi ne znači da je najbolje napravljeno.

---

## 2. dio: Dodatne funkcionalnosti

Želimo omogućiti dodavanje, ažuriranje i brisanje glavnih entiteta u aplikaciji (Album, Collector, Sticker).

### Album
- Dodavanje: Za kreiranje albuma može se opcionalno dati i lista postojećih sličica koje nemaju pridružen album.
- Ažuriranje: U slučaju da se prilikom ažuriranja albuma iz njega makne sličica, ta sličica se ne briše nego ostaje u sustavu da se može koristiti u nekom novom albumu.
- Brisanje: Ako se obriše album, sličice se ne brišu nego ostaju u sustavu da se mogu koristiti u nekom novom albumu

### Collector
- Dodavanje: Dodavanje novog korisnika
- Ažuriranje: Ažuriranje korisničkog imena
- Brisanje: Brisanje korisnika iz sustava

### Sticker
- Dodavanje: Dodavanje nove sličice, prilikom dodavanja može se ali ne mora navesti album kojem pripada sličica.
- Ažuriranje: Ažuriranjem sličice ne može se promijeniti album sličice.
- Brisanje: Brisanje sličice ne treba napraviti.

Naravno, želimo vidjeti i popis svih albuma i korisnika, te individualne prikaze za sva tri entiteta.  
Potrebno je implementirati ove funkcionalnosti tamo gdje ne postoje.

---

Želimo dodati ograničenja na otvaranje paketića u jednom danu. Jedan korisnik u jednom danu može otvoriti maksimalno 20 sličica.  
Unutar 24 sata može se otvoriti 20 sličica (00:00 - 23:59), ako se 20. sličica otvori u npr. 23:57, nove sličice mogu se otvarati već od 00:00.

---

Želimo dodati funkcionalnost dovršavanja albuma i nagraditi korisnike za njihov trud. Nakon što korisnik sakupi jedan primjerak svih sličica u pojedinom albumu šalje mu se notifikacija da je moguće taj album dovršiti.  
Dovršavanjem albuma korisnik gubi kopiju svake sličice iz tog albuma ali dobiva jednu "zvjezdicu".  
Jedan album može se dovršiti više puta.  
Zvjezdice se koriste za rangiranje korisnika po broju ispunjenih albuma.  
Potrebno je i omogućiti pregled "rang-liste" koja prikazuje 10 korisnika s najviše ispunjenih albuma u nekom mjesecu i godini.  
Treba omogućiti i pregled liste 10 korisnika s najviše ispunjenih albuma svih vremena.

---

Želimo postaviti određenu sigurnost tako da svaki korisnik ne može pristupati aplikaciji na isti način.  
Želimo in-memory autentifikaciju i autorizaciju.

U sustavu bi razlikovali 2 role: Collector i Admin.

- Collector: Ima pristup za čitanje svih albuma, sličica, korisnika.   
  Nema pristup dodavanju, ažuriranju ili brisanju nijednog entiteta.  
  Svaki collector u bazi ujedno je i korisnik s ulogom Collector u sustavu.  
  Za lakše testiranje postavite im svima password na "collector"

- Admin: Ima pristup administrativnim funkcijama u aplikaciji (Dodavanje novih albuma i sličica, brisanje entiteta, ažuriranje albuma i sličica, pregled svih entiteta).  
  Admin nije korisnik koji skuplja i otvara sličice pa ne smije ni imati pristup tim endpointima.  
  Postavite im password na "admin".

- Public: Kreiranje korisnika je endpoint za čiji se pristup ne treba autentificirati.

---

## Napomena
Nazivi i asercije u testovima su ispravne i prikazuju željeno ponašanje sustava, potrebno je mijenjati kod, a ne testove prilagođavati da rade.
