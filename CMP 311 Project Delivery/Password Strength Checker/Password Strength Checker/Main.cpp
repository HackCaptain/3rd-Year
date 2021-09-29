#include <iostream>                            // c++ standard libraries in
#include <cstdio>
#include <cassert>
#include <vector>			// Co-Star Data structure of the show
#include <string>			// Star data structure of the show
#include <chrono>			// The clock standard library
#include <cstring>			// Expanded String Library

#define Log(x)  cout << x << endl
#define LogIn(x) cout << x

using std::chrono::duration_cast;
using std::chrono::nanoseconds;							 // Setting up the clock to measure time in an appropriate format.

typedef std::chrono::steady_clock the_clock;             // Synchronises the clock

using namespace std;

int main() {

	string passwordstr = "default";

	char password[32];

	int length = 0;

	bool flag = 0;

	int newwordflag = 0;
	int wordcounter = 0;
	string word;

	int totalalpha = 0;
	int totalALPHA = 0;
	int totalnumber = 0;
	int totalsymbol = 0;

	int paircounter = 0;

	char firstchar;
	int repeatedcounter = 0;

	bool matchleft = 0;
	bool matchright = 0;

	int score = 0;


	#pragma region Populated Arrays
	char alphabet[26] = { char(97), char(98), char(99), char(100), char(101), char(102), char(103), char(104),
							char(105), char(106), char(107), char(108), char(109), char(110), char(111), char(112), char(113),
							char(114), char(115), char(116), char(117), char(118), char(119), char(120), char(121), char(122) };
	char ALPHABET[26] = { char(65), char(66), char(67), char(68), char(69), char(70), char(71), char(72),
							char(73), char(74), char(75), char(76), char(77), char(78), char(79), char(80), char(81),
							char(82), char(83), char(84), char(85), char(86), char(87), char(88), char(89), char(90) };
	char numbers[10] = { char(48), char(49), char(50), char(51), char(52), char(53), char(54), char(55), char(56), char(57) };
	char symbols[10] = { char(32), char(33), char(34), char(35), char(36), char(37), char(38), char(39), char(63), char(64) };

	char commonpairs[2][53]{
		{'t', 'h', 'a', 'i', 'e', 'n', 'r', 'e', 'e', 'o', 't', 'h', 'e', 'e', 's', 'n', 'o', 'a', 'h', 'a', 'i', 'n', 'i', 'o', 'e', 'o',
			't', 'a', 't', 's', 'm', 's', 'n', 'w', 'v', 'l', 'n', 't', 'a', 'd', 'o', 's', 'd', 'l', 't', 'e', 'r', 'a', 'd', 'e', 'r', 'r', 's'},
		{'h', 'e', 'n', 'n', 'r', 'd', 'e', 'd', 's', 'u', 'o', 'a', 'n', 'a', 't', 't', 'n', 't', 'i', 's', 't', 'g', 's', 'r', 't', 'f', 
			'i', 'r', 'e', 'e', 'e', 'a', 'e', 'a', 'e', 'e', 'o', 'a', 'l', 'e', 't', 'o', 't', 'l', 't', 'l', 'o', 'd', 'i', 'w', 'a', 'i', 'h'}
	};

	char subs[2][22]{
		{'@', '8', '(', '6', '3', '#', '9', '#', '1', '!', '<', '1', 'i', '0', '9', '5', '$', '+', '>', '<', '%', '?'},
		{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'i', 'k', 'l', 'l', 'o', 'q', 's', 's', 't', 'v', 'v', 'x', 'y'}
	};

	int alphacounter[26];
	int ALPHAcounter[26];
	int numbercounter[10];
	int symbolcounter[10];

	for (int a = 0; a < 26; a++) {
		alphacounter[a] = 0;
		ALPHAcounter[a] = 0;
	}

	for (int b = 0; b < 10; b++) {
		numbercounter[b] = 0;
		symbolcounter[b] = 0;
	}

	#pragma endregion Arrays contains all the populated arrays used in analaysis

	#pragma region Pairing Readout
	/*
	for (int a = 0; a < 53; a++) {
		Log("	Common Pairing " << a + 1 << " : ");
		for (int b = 0; b < 2; b++) {
			Log(commonpairs[b][a];
		}
		Log("\n");
	}
	*/
	#pragma endregion Pairing readout is used for debugging the pairing arrays
	
	#pragma region User Interaction

	Log("\n\n\n	Welcome to the password checking Program");
	Log("	ENTER YOUR PASSWORD BELOW FOR ANALYSIS");

	while (flag == 0){

		LogIn("\n---->	");
		std::getline(std::cin, passwordstr);
		Log("\n	Looking at: '" << passwordstr << "'");

		length = passwordstr.length();

		if (length < 32) {
			flag = 1;
		}
		else if (length > 32) {
			Log("	Password is too long\n");
		}
	}

	strcpy_s(password, passwordstr.c_str());			// String into char array

	#pragma endregion This is where the program takes in user input

	the_clock::time_point start = the_clock::now();		// Start timing

	Log("\n\n	----	Character Counting \n");

	#pragma region Character Counter
	//Character Counter_________________________________________________

	for (int i = 0; i < 26; i++) {					// Run through all Alphabet lists
		for (int j = 0; j < length; j++) {			// For each letter in the password
			if (password[j] == alphabet[i]) {
				//Log(" lowercase DETECTED " << alphabet[i] );
				alphacounter[i] = alphacounter[i] + 1;// Counter for lowercase
				totalalpha = totalalpha + 1;
			}
			if (password[j] == ALPHABET[i]) {
				//Log(" CAPITAL DETECTED " << ALPHABET[i] );
				ALPHAcounter[i] = ALPHAcounter[i] + 1;// Counter for CAPITAL
				totalALPHA = totalALPHA + 1;
			}
		}
	}

	for (int k = 0; k < 10; k++) {					// Run through all Number/ Symbol lists
		for (int l = 0; l < length; l++) {			// For each letter in the password
			if (password[l] == numbers[k]) {
				//Log(" number DETECTED " << numbers[k] );
				numbercounter[k] = numbercounter[k] + 1;// Counter for numbers 
				totalnumber = totalnumber + 1;
			}
			if (password[l] == symbols[k]) {
				//Log(" symbol DETECTED " << symbols[k] );
				symbolcounter[k] = symbolcounter[k] + 1;// Counter for symbols
				totalsymbol = totalsymbol + 1;
			}
		}
	}

	// Breakdown readout

	Log("	The password consists of \n");

	for (int m = 0; m < 26; m++) {					// Run through all Alphabet lists
		if (alphacounter[m] >= 1) {
			Log("	Character:	'" << alphabet[m] << "'		Amount: " << alphacounter[m]);
		}
		if (ALPHAcounter[m] >= 1) {
			Log("	Character:	'" << ALPHABET[m] << "'		Amount: " << ALPHAcounter[m]);
		}
	}

	for (int n = 0; n < 10; n++) {					// Run through all symbol lists
		if (numbercounter[n] >= 1) {
			Log("	Character:	'" << numbers[n] << "'		Amount: " << numbercounter[n]);
		}
		if (ALPHAcounter[n] >= 1) {
			Log("	Character:	'" << symbols[n] << "'		Amount: " << symbolcounter[n]);
		}
	}

	Log("	Therefore the password consists of in total: \n");

	Log("	Lowercase characters:	" << totalalpha);
	Log("	UPPERCASE characters:	" << totalALPHA);
	Log("	Numbers             :	" << totalnumber);
	Log("	Symbols             :	" << totalsymbol);

	// Suggestions and Grade

	if (totalALPHA < 2) {
		Log("	Your password contains a low amount of uppercase characters!");
	}
	else {
		score += 1;
	}
	if (totalnumber < 2) {
		Log("	Your password contains a low amount of numbers!");
	}
	else {
		score += 1;
	}
	if (totalsymbol < 2) {
		Log("	Your password contains a low amount of symbols!");
	}
	else {
		score += 1;
	}
	if (totalalpha > 10) {
		Log("	Your password contains a lot of lowercase!");
	}
	else {
		score += 1;
	}
	#pragma endregion Contains the code to breakdown the password

	Log("\n\n	----	Length Checking \n");

	#pragma region Length Checker
	//Length Checker____________________________________________________

	Log("	Length is : " << length);

	if (length == 1) {
	Log("	Thats... Thats a letter!	Password is non-existent... ERROR!!!");
	}
	else if (length < 5) {
		Log("	Password is very weak!");
	}
	else if (length < 10) {
		Log("	Password is weak!");
		score += 1;
	}
	else if (length < 15) {
		Log("	Password is average");
		score += 2;
	}
	else if (length < 20) {
		Log("	Password is good!");
		score += 3;
	}
	else if (length < 32) {
		Log("	Password is strong!");
		score += 4;
	}
	#pragma endregion Contains the code to check the length of the password

	Log("\n\n	----	Pair Counting \n");

	#pragma region Pairing Counter

	//Pairing Analysis___________________________________________________

	firstchar = password[0];

	for (int m = 1; m < length + 1; m++) {
		firstchar = password[m-1];
		if (firstchar == password[m]) {
			//repeatedpair.push_back( make_pair(firstchar, password[m]));
			LogIn("	Repeated character at " << m - 1 << "," << m << "	");
			Log(password[m] << "," << password[m - 1] << "	");
			repeatedcounter = repeatedcounter + 1;
		}
	}

	Log("	There are " << repeatedcounter << " repeated pairs \n");

	for (int u = 0; u < length - 1; u++) {															// For each letter in the password
		for (int v = 0; v < 53; v++) {																// and for each pair
			if (password[u] == commonpairs[0][v] && password[u + 1] == commonpairs[1][v] ) {		// look for a matching pair
				paircounter = paircounter + 1;														// if found add to counter

				LogIn("	Common Pair Detected at " << u << "," << u + 1 << "	");
				LogIn(password[u] << "," << password[u + 1] << "	");
				LogIn("Pair " << v << ": " << commonpairs[0][v] << commonpairs[1][v] << "\n");
			}
		}
	}

	Log("	There are " << paircounter << " common pairs \n");

	// Feedback and Score

	if ((paircounter > length / 2) || (repeatedcounter > length / 2))  {
		LogIn("	Your password is predictable");
		LogIn(" as more than half of the password is made of ");
		if (paircounter > length / 2) {
			LogIn("common pairs");
		}
		if ((paircounter > length / 2) && (repeatedcounter > length / 2)) {
			LogIn(" and ");
		}
		if (repeatedcounter > length / 2) {
			LogIn("repeated pairs");
		}
	}
	else if ((paircounter > length / 4) || (repeatedcounter > length / 4)) {
		LogIn("	Your password could be predictable");
		LogIn(" as more than a quarter of the password is made of ");
		if (paircounter > length / 4) {
			LogIn("common pairs");
			score += 2;
		}
		if ((paircounter > length / 4) && (repeatedcounter > length / 4)) {
			LogIn(" and ");
		}
		if (repeatedcounter > length / 4) {
			LogIn("repeated pairs");
			score += 2;
		}
	}
	else if ((paircounter < length / 4) || (repeatedcounter < length / 4)) {
		LogIn("	Your password is likely unpredictable");
		LogIn(" as less than a quarter of the password is made of ");
		if (paircounter < length / 4) {
			LogIn("common pairs");
			score += 4;
		}
		if ((paircounter < length / 4) && (repeatedcounter < length / 4)) {
			LogIn(" and ");
		}
		if (repeatedcounter < length / 4) {
			LogIn("repeated pairs");
			score += 4;
		}
	}
	Log("\n");
	//else if (paircounter > length / 4) {
	//	Log("	Your password could be predictable");
	//	Log("	as more than a quarter of the password is made of common pairs");
	//	score += 2;
	//}
	//else if (paircounter < length / 4) {
	//	Log("	Your password is unlikely predictable");
	//	Log("	as less than a quarter of the password is 'random'");
	//	score += 4;
	//}
	//if (repeatedcounter > length / 2) {
	//	Log("	Your password is predictable");
	//	Log("	as more than half of the password is made of repeated pairs");
	//}
	//else if (repeatedcounter > length / 4) {
	//	Log("	Your password could be predictable");
	//	Log("	as more than a quarter of the password is made of common pairs");
	//	score += 2;
	//}
	//else if (repeatedcounter < length / 4) {
	//	Log("	Your password is unlikely predictable");
	//	Log("	as less than a quarter of the password is 'random'");
	//	score += 4;
	//}
	//Log("\n");

	#pragma endregion counts the number of common pairs in the password

	Log("\n\n	----	Word Counting \n");

	#pragma region Word Counter

	//Word Detector__________________________________

	vector<string> words;												// Declare a vector of stings

	int w = 0;
	while (w < length + 1) {																	// As program parses the password, while the character in focus is not the last (redundant?)
			if (password[w] == char(32) || password[w] == char(95) || w == length) {			// Checking if the next character is a (valid) space (this should be improved to cover other subs)
				for (int x = newwordflag; x < w; x++) {											// Begin creating a container
					word.push_back(password[x]);												// Push back each character in the detected word
				}

				words.push_back(word);					// Save the detected word
				word.clear();							// And clear this container for future use

				//w = w + 1;							// Skip to the next valid character

				wordcounter = wordcounter + 1;			// Add a word to the wordscounter
				newwordflag = w + 1;					// Move the start of the next word to the next valid word
			}
			w++;										// We've checked this letter, move along the string
	}

	if (wordcounter > 0) {
		Log("	The supplied password has " << wordcounter << " words in it : \n");

		for (int y = 0; y < wordcounter; y++) {
			LogIn("	Word:	" << y + 1);
			LogIn(" : " << words[y] <<  "\n" );
		}
	}

	#pragma endregion counts and stores detected words


	Log("\n\n	----	Substitution Counting \n");

	#pragma region Sub Counter?

	for (int f = 1; f < length + 1; f++) {					// Run through Password
		for (int g = 0; g < 22; g++) {						// Run through substitutions

			//Log("		Checking subs " << g + 1 << "	: " << subs[0][g] << "," << subs[1][g] << " On letter " << f );

			if (password[f] == subs[0][g]) {				// Detect a subs character

				Log("	Substituted Character detected at position " << f << ", character : '" << password[f] << "'");

				for (int h = 0; h < 53; h++) {				// Run through list of common pairings to find correct pairing
						// Then check against character left for what a common pairing would like w'out subs
					if ((subs[1][g] == commonpairs[1][h] && password[f - 1] == commonpairs[0][h]) && f - 1 > -1) {		// look for a matching pair on the left without leaking
						matchleft = 1;
					}				
						// Then check against character right for what a common pairing would like w'out subs
					if (subs[1][g] == commonpairs[0][h] && password[f + 1] == commonpairs[1][h]) {
						matchright = 1;
					}
				}

				// Readout

				if (matchleft == 1 || matchright == 1) {
					if (matchleft == 1 && matchright == 1) {
						LogIn("	Likely Substituted Character detected at " << f - 1 << "," << f << "," << f + 1);
						Log(" '" << password[f - 1] << "', '" << password[f] << "', '" << password[f + 1] << "'");
					}
					else if (matchleft == 1) {
						Log("	Possible Substituted Character detected at " << f - 1 << "," << f << "	");
					}
					else if (matchright == 1){
						Log("	Possible Substituted Character detected at " << f << "," << f + 1 << "	");
					}
				}
				if (matchleft == 0 && matchright == 0) {
					Log("	Character detected at " << f << ", '" << password[f] << "'	Adds to Entropy");
				}
				matchleft = 0;
				matchright = 0;
			}
		}

		//Counter

	}

	// For each symbol and select number
	// check neighbouring chars
	//	For password
	// For symbols/numbers
	//		if leftchar & targetchar = common pairing L, substitution
	//			if targetchar and rightchar = substitution and common paring R
	//				Substitution + 1
	//			else 
	//				possible substitution + 1
	//		else
	//			randomsymbol + 1
		

	#pragma endregion Detects and counts possible common symbol substitution

	Log("\n\n	Your total score for this password is: " << score << " / 16 \n");

	the_clock::time_point end = the_clock::now();		// Stop timing

	auto time_taken = duration_cast<nanoseconds>(end - start).count();		// Work out time taken and display the result
	Log("\n\n	Program took " << time_taken << " nanoseconds\n");

	// Add repeated pair checking - Yes
	// Natural Language detection - more or less
	// Common word detection - sorta

	// Length to time Brute force calc 
	// Account for entropy
	// Maybe replace the character counters with selection that creates / modifies and pushes back a pair
}