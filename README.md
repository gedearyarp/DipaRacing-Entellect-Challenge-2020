# Dipa Racing - Entellect Challenge 2020

## Greedy Algorithm
Strategi greedy pada alternatif ini adalah pada setiap langkah melihat block mana saja yang akan dilewati. Terdapat beberapa kasus untuk konfigurasi block yang akan dilewati yaitu ketika melakukan TURN_LEFT, TURN_RIGHT, USE_LIZARD, ACCELERATE, DECCELERATE, USE_BOOST, dan command sisanya yang akan membuat mobil maju dengan speed sekarang. Jadi terdapat 7 kemungkinan kombinasi block yang dilewati dalam 1 turn. Kombinasi block ini akan diberi weight setiap blocknya. Parameter yang diperhatikan antara lain adalah kondisi mobil setelah melewati block-block tersebut seperti berapakah speed akhir mobil, damage akhir mobil, jumlah block maju, dan apakah terdapat boost pada block tersebut. Pemilihan command didasarkan pada yang memiliki weight terbaik, jika weight lurus (tanpa ACCELERATE) yang paling baik baru pilih command menyerang terbaik.

Bot baru akan melakukan offensive ketika arah yang dipilih lebih baik lurus tanpa melakukan ACCELERATE ataupun USE_BOOST. Pemilihan command ini dilakukan dalam fungsi offensive. Offensive di sini dibagi ketika kita di depan lawan dan di belakang lawan. Ketika di belakang lawan dan selisih lane dengan lawan kurang sama dengan satu, akan digunakan EMP. Jika kita di depan lawan akan diprioritaskan menggunakan TWEET jika ada. TWEET diletakkan dengan memprediksi speed lawan selanjutnya, lalu diletakkan pada block awal lawan + prediksi speed lawan + 1. Jika tidak ada TWEET baru digunakan OIL.



## Requirement
* JDK 1.8
* Apache Maven
* Clone repository ini
* Download starter-pack di https://github.com/EntelectChallenge/2020-Overdrive/releases/tag/2020.3.4

## Build and Run
* Download starter-pack di https://github.com/EntelectChallenge/2020-Overdrive/releases/tag/2020.3.4
* Clone repository ini dan copy isi folder src dan replace isi folder starter-pack/starter-bots/java
* Build dengan maven akan menghasilkan folder target dan file .jar (File yang sama dengan file yang berada di folder bin repo ini)
* Ubah konfigurasi bot.json menjadi
```
"botFileName": "MandalikaPertaminaDipaRacing-jar-with-dependencies.jar",
```
(Pada repo ini file MandalikaPertaminaDipaRacing-jar-with-dependencies.jar sudah direname menjadi MandalikaPertaminaDipaRacing.jar yang terdapat pada folder bin)
* Ubah game-runner-config.json di folder starter-pack menjadi
```
  "player-a": "./starter-bots/java",
  "player-b": "./reference-bot/java",
```
* Jalankan file run.bat

## Useful Links
* Repo Utama: https://github.com/EntelectChallenge/2020-Overdrive
* Rules: https://github.com/EntelectChallenge/2020-Overdrive/blob/develop/game-engine/game-rules.md
* How to run locally: https://github.com/EntelectChallenge/2020-Overdrive/blob/develop/game-runner/README.md
* Starter Pack: https://github.com/EntelectChallenge/2020-Overdrive/releases/tag/2020.3.4
* What is state-file: https://github.com/EntelectChallenge/2020-Overdrive/blob/master/game-engine/state-files.md
* Visualizers: https://entelect-replay.raezor.co.za/

## Video
* https://youtu.be/hViNhhYMC8A

## Authors
* I Gede Arya Raditya Parameswara (13520036)
* Arik Rayi Arkananta (13520048)
* Ubaidillah Ariq Prathama (13520085)
