# Benchmark Corpus Manifest

Location: `data/ground_truth/` (gitignored, ~1GB). Zips = pristine backups.

Generated 2026-07-05 from Info.dat scan. `varBPM` = count of real mid-song BPM changes (≠ base). Maps with varBPM>0 are **excluded from onset F-measure v1** (piecewise beat→sec conversion not yet implemented) but still used for BPM-detection eval.

| id | song | mapper | BPM | dur (s) | notes (X+) | NPS | varBPM |
|---|---|---|---|---|---|---|---|
| 11ed8 | Hoihoi*Gensouholoism | Rusty | 195 | 273.1 | 2358 | 8.63 | 0 |
| 13735 | SAtAN | Sombra, Fnyt & Slams | 300 | 122.0 | 1458 | 11.95 | 0 |
| 138ba | True Hero | Cerret, Complex & Da | 269 | 137.8 | 1466 | 10.64 | 0 |
| 168de | Der Herrgott hot glocht | MonsterWook, FatBean | 200 | 175.6 | 1610 | 9.17 | 0 |
| 1703f | Party People | Helloiamdaan & Nolan | 350 | 186.1 | 2182 | 11.72 | 0 |
| 188ed | The Everlasting Calamity That Shift | Jabob & Helloiamdaan | 286.5 | 144.6 | 2024 | 14.0 | 12 |
| 1a15 | Nisemono Chuuihou | Hexagonial (SEE BEAT | 260 | 221.9 | 1924 | 8.67 | 0 |
| 1a2cd | Fantasie-Celeritas | ComplexFrequency | 350 | 178.3 | 2073 | 11.62 | 0 |
| 1a32a | Oyasumi | Alice | 200 | 140.0 | 907 | 6.48 | 0 |
| 1a938 | Revenant | Shan_Man | 210 | 193.1 | 1766 | 9.14 | 0 |
| 1a939 | Maware! Setsugetsuka  | BigSlick | 160 | 232.2 | 1828 | 7.87 | 0 |
| 1ac0f | p.h. | TOFU | 132 | 155.0 | 876 | 5.65 | 0 |
| 1ad66 | Thunderstrike | abcbadq & Timbo | 256 | 308.3 | 2734 | 8.87 | 0 |
| 1d865 | God-ish -covered by Sekai- | ani vs salami | 142 | 205.4 | 1309 | 6.37 | 1 |
| 1e941 | The Curse Of The Lovely Fox | abcbadq | 260 | 270.2 | 2562 | 9.48 | 0 |
| 1ffbf | Tanz mit mir | AntiLink | 192 | 183.7 | 897 | 4.88 | 0 |
| 22c55 | I'm getting on the bus to the other | WDG_Plasim | 280 | 159.0 | 2014 | 12.67 | 0 |
| 23782 | Palace of Melancholia | Jabob & Joshabi | 227 | 347.3 | 3645 | 10.49 | 1 |
| 265a5 | Apocrypha | Slayx | 236 | 119.8 | 1041 | 8.69 | 0 |
| 282d8 | 99.9 | Voidless & Dailyy | 250 | 128.3 | 1443 | 11.25 | 0 |
| 289ab | Last Wish | BSWC Team | 210 | 333.6 | 3681 | 11.03 | 12 |
| 28a58 | Last Wish | Nolanimations | 210 | 333.6 | 3444 | 10.32 | 12 |
| 297b5 | GHOST | GojiCringer | 220 | 347.0 | 2375 | 6.84 | 0 |
| 29d0e | Out of This Planet | Cat Using A Toaster | 205 | 154.9 | 1333 | 8.61 | 0 |
| 2c9bf | Sister's Noise | anammelech | 388 | 147.8 | 1572 | 10.63 | 0 |
| 2d4e6 | Religion | A Jhintleman & GojiC | 130 | 334.9 | 1981 | 5.92 | 0 |
| 2ddb9 | Ashed Wings | DiscoBaIIerz | 240 | 284.6 | 3149 | 11.06 | 6 |
| 2de76 | Diabolic Swing | HOFNutCollector | 256 | 152.3 | 1966 | 12.9 | 0 |
| 2e0d0 | BS Recall | Cube Community Team | 200 | 306.6 | 2597 | 8.47 | 4 |
| 2f141 | Gigantic O.T.N | Hexagonial | 190 | 199.5 | 2044 | 10.25 | 0 |
| 2f951 | Hello (BPM) 2023 | Yasu | 252.875 | 161.8 | 2407 | 14.87 | 0 |
| 2ff94 | Bayonex - "Xtrablast / V1.1" Long V | Fnyt | 262 | 242.7 | 2795 | 11.51 | 0 |
| 30113 | Yokatta | DiscoBaIIerz | 200 | 206.2 | 2452 | 11.89 | 0 |
| 3036 | Milk Crown on Sonnetica | Hexagonial | 254.9199981689453 | 216.5 | 1944 | 8.98 | 0 |
| 3079f | parrying a rapid​-​fire combo (it's | DiscobaIIerz | 222.0 | 326.5 | 2694 | 8.25 | 0 |
| 31d13 | Luminency | Fnyt | 350 | 265.4 | 2648 | 9.98 | 0 |
| 31d3f | KOKUSHIMUSOU | HOFNutCollector | 340 | 195.2 | 2416 | 12.38 | 0 |
| 32180 | Unlock the Blacksky | FentonVR | 212 | 153.8 | 1424 | 9.26 | 0 |
| 328aa | KOKUSHIMUSOU | Aquaflee | 340.0 | 200.1 | 2247 | 11.23 | 0 |
| 33bd9 | Virtual Ragnarok Chat | BlAck_vOid-1001 | 290.0 | 212.8 | 2336 | 10.98 | 0 |
| 3442c | Villain Virus | GalaxyMaster & GojiC | 260.0 | 268.9 | 2613 | 9.72 | 0 |
| 368f4 | Shukusei!! Loli Kami Requiem | Uragirimono | 165.0 | 275.7 | 2187 | 7.93 | 0 |
| 36a4e | Insane | Cush | 105.0 | 162.2 | 800 | 4.93 | 0 |
| 3ae5 | Re: End of a Dream | Scrappy | 212 | 147.0 | 1171 | 7.97 | 0 |
| 54b3 | Glucagon | BananenTropfen | 274 | 204.4 | 2042 | 9.99 | 0 |
| 7829 | psychology | Scrappy | 306 | 216.7 | 2011 | 9.28 | 1 |

**Stats:** 46 maps | BPM 105-388 | 33 maps >200 BPM | 8 variable-BPM (excluded from onset eval) | 38 usable for onset F-measure | total ~166 min audio.

Corpus profile: mostly EDM/electronic, ranked/curated maps, varied mapping styles, all have ExpertPlus. More available on request (ranked >2024, curated).
