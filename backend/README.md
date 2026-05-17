## 1.
Svaki album ima barem jednu sličicu. Sličice sadrže edukativne informacije o životinji koju predstavljaju (ime, latinsko ime, stanište, opis, slika).  
Različiti albumi ne mogu sadržavati istu sličicu.


### 3.
Korisnici mogu međusobno razmjenjivati sličice. Postoje dva načina razmjene sličica: globalni i osobni.  
Razmjena sličica obavlja se preko zahtjeva za razmjenu. Korisnik kojemu je ponuđen zahtjev može prihvatiti ili odbiti zahtjev za razmjenu.  
Kada korisnik zatraži globalnu razmjenu svi korisnici koji imaju traženu sličicu viška, a nemaju niti jednu ponuđenu dobiju zahtjev za razmjenu.  
Zahtjev za osobnom razmjenom šalje se korisniku koji je naveden pri kreiranju zahtjeva.  
Pri osobnoj razmjeni može se zatražiti bilo koja sličica koju korisnik ima u kolekciji.  
Dozvoljena je razmjena sličica iz različitih albuma.


### 5.
Korisnicima dolaze notifikacije. Notifikacija sadrži identifikator korisnika kojem je namijenjena, poruku i datum nastajanja.  
Notifikacije se šalju kad korisnik primi novi zahtjev za razmjenu ili korisnikov zahtjev bude odbijen/prihvaćen.  
Potrebno je i pratiti koje je notifikacije korisnik pročitao i omogućiti dohvaćanje samo ne pročitanih notifikacija.

### Album
- Dodavanje: Za kreiranje albuma može se opcionalno dati i lista postojećih sličica koje nemaju pridružen album.
- Ažuriranje: U slučaju da se prilikom ažuriranja albuma iz njega makne sličica, ta sličica se ne briše nego ostaje u sustavu da se može koristiti u nekom novom albumu.
- Brisanje: Ako se obriše album, sličice se ne brišu nego ostaju u sustavu da se mogu koristiti u nekom novom albumu

### Sticker
- Dodavanje: Dodavanje nove sličice, prilikom dodavanja može se ali ne mora navesti album kojem pripada sličica.
- Ažuriranje: Ažuriranjem sličice ne može se promijeniti album sličice.
- Brisanje: Brisanje sličice ne treba napraviti.

Naravno, želimo vidjeti i popis svih albuma i korisnika, te individualne prikaze za sva tri entiteta.  
Potrebno je implementirati ove funkcionalnosti tamo gdje ne postoje.


Želimo dodati funkcionalnost dovršavanja albuma i nagraditi korisnike za njihov trud. Nakon što korisnik sakupi jedan primjerak svih sličica u pojedinom albumu šalje mu se notifikacija da je moguće taj album dovršiti.  
Dovršavanjem albuma korisnik gubi kopiju svake sličice iz tog albuma ali dobiva jednu "zvjezdicu".  
Jedan album može se dovršiti više puta.  
Zvjezdice se koriste za rangiranje korisnika po broju ispunjenih albuma.  
Potrebno je i omogućiti pregled "rang-liste" koja prikazuje 10 korisnika s najviše ispunjenih albuma u nekom mjesecu i godini.  
Treba omogućiti i pregled liste 10 korisnika s najviše ispunjenih albuma svih vremena.

