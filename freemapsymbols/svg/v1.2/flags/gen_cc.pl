#!/usr/bin/perl -w
#
use strict;

my $nations_dir = 'nations';
my $codes_dir = 'cc';
my $hard_link = 1;
my $debug = 0;

# Non-standart or not in $nations_dir flags
my %extra = (
    'aq' => 'Regional/antarctica.svg',
    'fo' => 'Regional/denmark_faroe_islands.svg',
    'eu' => 'Organizations/europeanunion.svg',
    'gi' => 'Regional/uk_gibraltar.svg',
    'gl' => 'Regional/denmark_grenland.svg',
    'hk' => 'Regional/china_hong_kong.svg',
    'io' => 'Regional/british_indian_ocean_territory.svg',
    'mo' => 'Regional/china_macau.svg',
# !   'mq' => 'Regional/france_martinique.svg',
    'nf' => 'Regional/australia_norfolk_island_australia.svg',
    'pf' => 'Regional/france_french_polynesia.svg',
    'tw' => 'Regional/taiwan.svg',
    'su' => 'historic/ussr_historic.svg',
    'ps' => 'Organizations/palestine.svg',
# !   'wf' => 'Regional/france_wallis_and_futuna.svg',
);

# Hardcode some of the names
my %cc = (
    'bv' => 'norway',
    'cd' => 'congo_kinshasa',
    'cg' => 'congo-brazzaville',# congo_brazzaville
    'cc' => 'australia',
    'ch' => 'suisse',		# switzerland
    'gf' => 'france',
    'gp' => 'france',
    'gw' => 'guineabissau',	# guinea_bissau
    'hm' => 'australia',
    'im' => 'isle_of_man',	# Unofficial!
    'kp' => 'north_korea',
    'kr' => 'south_korea',
    'la' => 'laos',
    'mq' => 'france',		# !
    'nc' => 'france',
    'pm' => 'france',		# !
    're' => 'france',
    'tf' => 'france',
    'sj' => 'norway',
    'um' => 'united_states',
    'va' => 'vatican',
    'vg' => 'virgin_islands_gb',
    'vi' => 'virgin_islands_us',
    'wf' => 'france',
    'yt' => 'france',
);

while(<DATA>) {
    if(/^\s*(.*?);(\w\w)\s*$/) {
	my($name, $cc) = ($1, $2);
	$cc = lc($cc);
	
	unless($cc{$cc}) {
	    $name = lc($name);
	    $name =~ s/\'//g;
	    $name =~ s/\([^\)]+\)//g;
	    $name =~ s/ +/_/g;
	    $name =~ s/-+/_/g;
	    $name =~ s/^_+//;
	    $name =~ s/_+$//;
	    $name =~ s/(.*?),.*$/$1/g;
	    $cc{$cc} = $name;
	}
    }
}

# Collect list of files
opendir(DIR, $nations_dir);
my @files = grep { s/\.svg//; } readdir(DIR);
closedir(DIR);

mkdir($codes_dir) unless(-d $codes_dir);

foreach my $cc (keys(%extra)) {
    my $from = $extra{$cc};
    my $to = "${codes_dir}/${cc}.svg";
    if($hard_link) {
	link($from, $to) || warn("Error linking $from to $to: $!\n");
    }
    else {
    	symlink("../$from", $to) || warn("Error linking ../$from to $to: $!\n");
    }
    delete($cc{$cc});
}

foreach my $country (@files) {
    my $was_used = 0;
    foreach my $cc (keys(%cc)) {
	if($cc{$cc} eq $country) {
	    my $from = "${nations_dir}/${country}.svg";
	    my $to = "${codes_dir}/${cc}.svg";
	    if($hard_link) {
		link($from, $to) || warn("Error linking $from to $to: $!\n");
	    }
	    else {
		symlink("../$from", $to) || warn("Error linking ../$from to $to: $!\n");
	    }
	    delete($cc{$cc});
	    $was_used = 1;
	}
    }
    undef($country) if($was_used);
}

if($debug) {
    foreach my $cc (sort(keys(%cc))) {
	print "$cc\t", $cc{$cc}, "\n";
    }
    foreach my $country (@files) {
	print "$country\n" if($country);
    }
}

__END__
http://www.iso.org/iso/en/prods-services/iso3166ma/02iso-3166-code-lists/list-en1-semic.txt
This list states the country names (official short names in English) in alphabetical order as given in ISO 3166-1 and the corresponding ISO 3166-1-alpha-2 code elements. The list is updated whenever a change to the official code list in ISO 3166-1 is effected by the ISO 3166/MA. It lists 239 official short names and code elements. One line of text contains one entry. A country name and its code element are separated by a semicolon (;).

AFGHANISTAN;AF
ALBANIA;AL
ALGERIA;DZ
AMERICAN SAMOA;AS
ANDORRA;AD
ANGOLA;AO
ANGUILLA;AI
ANTARCTICA;AQ
ANTIGUA AND BARBUDA;AG
ARGENTINA;AR
ARMENIA;AM
ARUBA;AW
AUSTRALIA;AU
AUSTRIA;AT
AZERBAIJAN;AZ
BAHAMAS;BS
BAHRAIN;BH
BANGLADESH;BD
BARBADOS;BB
BELARUS;BY
BELGIUM;BE
BELIZE;BZ
BENIN;BJ
BERMUDA;BM
BHUTAN;BT
BOLIVIA;BO
BOSNIA AND HERZEGOVINA;BA
BOTSWANA;BW
BOUVET ISLAND;BV
BRAZIL;BR
BRITISH INDIAN OCEAN TERRITORY;IO
BRUNEI DARUSSALAM;BN
BULGARIA;BG
BURKINA FASO;BF
BURUNDI;BI
CAMBODIA;KH
CAMEROON;CM
CANADA;CA
CAPE VERDE;CV
CAYMAN ISLANDS;KY
CENTRAL AFRICAN REPUBLIC;CF
CHAD;TD
CHILE;CL
CHINA;CN
CHRISTMAS ISLAND;CX
COCOS (KEELING) ISLANDS;CC
COLOMBIA;CO
COMOROS;KM
CONGO;CG
CONGO, THE DEMOCRATIC REPUBLIC OF THE;CD
COOK ISLANDS;CK
COSTA RICA;CR
COTE D'IVOIRE;CI
CROATIA;HR
CUBA;CU
CYPRUS;CY
CZECH REPUBLIC;CZ
DENMARK;DK
DJIBOUTI;DJ
DOMINICA;DM
DOMINICAN REPUBLIC;DO
ECUADOR;EC
EGYPT;EG
EL SALVADOR;SV
EQUATORIAL GUINEA;GQ
ERITREA;ER
ESTONIA;EE
ETHIOPIA;ET
FALKLAND ISLANDS (MALVINAS);FK
FAROE ISLANDS;FO
FIJI;FJ
FINLAND;FI
FRANCE;FR
FRENCH GUIANA;GF
FRENCH POLYNESIA;PF
FRENCH SOUTHERN TERRITORIES;TF
GABON;GA
GAMBIA;GM
GEORGIA;GE
GERMANY;DE
GHANA;GH
GIBRALTAR;GI
GREECE;GR
GREENLAND;GL
GRENADA;GD
GUADELOUPE;GP
GUAM;GU
GUATEMALA;GT
GUINEA;GN
GUINEA-BISSAU;GW
GUYANA;GY
HAITI;HT
HEARD ISLAND AND MCDONALD ISLANDS;HM
HOLY SEE (VATICAN CITY STATE);VA
HONDURAS;HN
HONG KONG;HK
HUNGARY;HU
ICELAND;IS
INDIA;IN
INDONESIA;ID
IRAN, ISLAMIC REPUBLIC OF;IR
IRAQ;IQ
IRELAND;IE
ISRAEL;IL
ITALY;IT
JAMAICA;JM
JAPAN;JP
JORDAN;JO
KAZAKHSTAN;KZ
KENYA;KE
KIRIBATI;KI
KOREA, DEMOCRATIC PEOPLE'S REPUBLIC OF;KP
KOREA, REPUBLIC OF;KR
KUWAIT;KW
KYRGYZSTAN;KG
LAO PEOPLE'S DEMOCRATIC REPUBLIC;LA
LATVIA;LV
LEBANON;LB
LESOTHO;LS
LIBERIA;LR
LIBYAN ARAB JAMAHIRIYA;LY
LIECHTENSTEIN;LI
LITHUANIA;LT
LUXEMBOURG;LU
MACAO;MO
MACEDONIA, THE FORMER YUGOSLAV REPUBLIC OF;MK
MADAGASCAR;MG
MALAWI;MW
MALAYSIA;MY
MALDIVES;MV
MALI;ML
MALTA;MT
MARSHALL ISLANDS;MH
MARTINIQUE;MQ
MAURITANIA;MR
MAURITIUS;MU
MAYOTTE;YT
MEXICO;MX
MICRONESIA, FEDERATED STATES OF;FM
MOLDOVA, REPUBLIC OF;MD
MONACO;MC
MONGOLIA;MN
MONTSERRAT;MS
MOROCCO;MA
MOZAMBIQUE;MZ
MYANMAR;MM
NAMIBIA;NA
NAURU;NR
NEPAL;NP
NETHERLANDS;NL
NETHERLANDS ANTILLES;AN
NEW CALEDONIA;NC
NEW ZEALAND;NZ
NICARAGUA;NI
NIGER;NE
NIGERIA;NG
NIUE;NU
NORFOLK ISLAND;NF
NORTHERN MARIANA ISLANDS;MP
NORWAY;NO
OMAN;OM
PAKISTAN;PK
PALAU;PW
PALESTINIAN TERRITORY, OCCUPIED;PS
PANAMA;PA
PAPUA NEW GUINEA;PG
PARAGUAY;PY
PERU;PE
PHILIPPINES;PH
PITCAIRN;PN
POLAND;PL
PORTUGAL;PT
PUERTO RICO;PR
QATAR;QA
REUNION;RE
ROMANIA;RO
RUSSIAN FEDERATION;RU
RWANDA;RW
SAINT HELENA;SH
SAINT KITTS AND NEVIS;KN
SAINT LUCIA;LC
SAINT PIERRE AND MIQUELON;PM
SAINT VINCENT AND THE GRENADINES;VC
SAMOA;WS
SAN MARINO;SM
SAO TOME AND PRINCIPE;ST
SAUDI ARABIA;SA
SENEGAL;SN
SERBIA AND MONTENEGRO;CS
SEYCHELLES;SC
SIERRA LEONE;SL
SINGAPORE;SG
SLOVAKIA;SK
SLOVENIA;SI
SOLOMON ISLANDS;SB
SOMALIA;SO
SOUTH AFRICA;ZA
SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS;GS
SPAIN;ES
SRI LANKA;LK
SUDAN;SD
SURINAME;SR
SVALBARD AND JAN MAYEN;SJ
SWAZILAND;SZ
SWEDEN;SE
SWITZERLAND;CH
SYRIAN ARAB REPUBLIC;SY
TAIWAN, PROVINCE OF CHINA;TW
TAJIKISTAN;TJ
TANZANIA, UNITED REPUBLIC OF;TZ
THAILAND;TH
TIMOR-LESTE;TL
TOGO;TG
TOKELAU;TK
TONGA;TO
TRINIDAD AND TOBAGO;TT
TUNISIA;TN
TURKEY;TR
TURKMENISTAN;TM
TURKS AND CAICOS ISLANDS;TC
TUVALU;TV
UGANDA;UG
UKRAINE;UA
UNITED ARAB EMIRATES;AE
UNITED KINGDOM;GB
UNITED STATES;US
UNITED STATES MINOR OUTLYING ISLANDS;UM
URUGUAY;UY
UZBEKISTAN;UZ
VANUATU;VU
VENEZUELA;VE
VIET NAM;VN
VIRGIN ISLANDS, BRITISH;VG
VIRGIN ISLANDS, U.S.;VI
WALLIS AND FUTUNA;WF
WESTERN SAHARA;EH
YEMEN;YE
ZAMBIA;ZM
ZIMBABWE;ZW
