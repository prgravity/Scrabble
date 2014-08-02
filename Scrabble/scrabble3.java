package com.scrabble;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class scrabble3 {

	static List<Integer> spaceBlanks = new LinkedList<Integer>();
	static List<String> existingLetters = new LinkedList<String>();
	static String rack = "";
	static String constraint = new String();
	static boolean hasBlank = false;

	public static void main(String[] args) throws IOException {
		ArrayList<String> input = readInputFile("input.txt");
		HashMap<Long, ArrayList<String>> mapOfWords = new HashMap<Long, ArrayList<String>>();
		for (int i = 0; i < input.size(); i++) {
			hasBlank = false;
			processInput(input.get(i));
			mapOfWords = findMaxScoringWord( mapOfWords);
			printMaxWord(mapOfWords);
			mapOfWords = new HashMap<Long, ArrayList<String>>();
		}
	}
	
	
	private static HashMap<Long, ArrayList<String>> findMaxScoringWord( HashMap<Long, ArrayList<String>> matchedWords) throws IOException{
		ArrayList<String> matchingWords = new ArrayList<String>();
		if (rack.length() == 6) {
			hasBlank = true;
			addExistingLettersToRack();
			String regex = createRegex(true);
			matchingWords.addAll(findMatchingWords(regex));
			matchedWords = computeScrabbleScores(matchingWords);
			matchedWords = filterMatchedWords(matchedWords);
		} else {
			String regex = createRegex(false);
			matchingWords.addAll(findMatchingWords(regex));
			addExistingLettersToRack();
			matchedWords = computeScrabbleScores(matchingWords);
			matchedWords = filterMatchedWords(matchedWords);
		}
		return matchedWords;
	}

	
	private static void addExistingLettersToRack() {
		for(String letter : existingLetters){
			rack+=letter;
		}
	}

	
	private static String createRegex(boolean hasBlankTile) {
		String regex = new String("");
		if (constraint.charAt(0) != '_') {
			spaceBlanks.add(0, 0);
		}
		if (constraint.charAt(constraint.length() - 1) != '_') {
			spaceBlanks.add(0);
		}
		if (existingLetters.size() == 0) {
			existingLetters.add("");
		}
		String newRack = rack;
		if(hasBlankTile){
			String addBlankTileRegex = new String("(");
			for (char blankTile = 'A'; blankTile < 'Z'; blankTile++){
				addBlankTileRegex += String.valueOf(blankTile) + "|";
			}
			addBlankTileRegex += "Z)";
			newRack += addBlankTileRegex; 
		} 
		regex += "[" + newRack + "]" + "{" + 0 + "," + spaceBlanks.get(0) + "}"
				+ existingLetters.get(0);
		for (int index = 1; index < spaceBlanks.size() - 1; index++) {
			regex += "[" + newRack + "]" + "{" + spaceBlanks.get(index) + "}"
					+ existingLetters.get(index);
		}

		regex += "[" + newRack + "]" + "{" + 0 + ","
				+ spaceBlanks.get(spaceBlanks.size() - 1) + "}";
		return regex;
	}

	
	static void processInput(String input) {
		existingLetters = new LinkedList<String>();
		spaceBlanks = new LinkedList<Integer>();
		char[] constraintAsArray;
		rack = input.split(" ")[1];
		rack.toUpperCase();
		constraint = input.split(" ")[0];
		constraintAsArray = constraint.toCharArray();
		
		int count = 0;
		for (int k = 0; k < constraint.length(); k++) {

			if (constraintAsArray[k] != '_') {
				existingLetters.add(Character.toString(constraintAsArray[k]));
				spaceBlanks.add(count);
				count = 0;
			} else
				count++;

		}
		spaceBlanks.add(count);
	} 

	static boolean checkSequence(String rack, String dictionaryWord){ 
		Boolean blankTile = false;
		for (int i = 0; i < dictionaryWord.length(); i++) {
			String charOfWord = "" + dictionaryWord.charAt(i);
			String tempRack = rack.replaceFirst(charOfWord, "");
			if (tempRack.length() == rack.length()) {
				if ( hasBlank && blankTile == false) {
					blankTile = true;
				} else
					return false;
			}
			rack = tempRack;
		}
		return true;
	}

	
	static HashMap<Long, ArrayList<String>> filterMatchedWords(HashMap<Long, ArrayList<String>> mapOfWords) {
		long key;
		ArrayList<String> value;
		List<Long> removeKeys = new LinkedList<Long>();
		for (Entry<Long, ArrayList<String>> entry : mapOfWords.entrySet()) {
			key = entry.getKey();
			value = entry.getValue();
			for (int i = 0; i < value.size(); i++) {
				if (!checkSequence(rack, value.get(i))) {
					value.remove(i--);
				}
			}
			if (value.size() > 0) {
				mapOfWords.put(key, value);
			} else {
				removeKeys.add(key);
			}
		}
		for (Long keyToRemove : removeKeys) {
			mapOfWords.remove(keyToRemove);
		}
		return mapOfWords;
	}

	
	static void printMaxWord(HashMap<Long, ArrayList<String>> mapOfWords) {
		long score = -1L;
		ArrayList<String> value = new ArrayList<String>();
		ArrayList<String> maxValue = new  ArrayList<String>();
		for (Entry<Long, ArrayList<String>> entry : mapOfWords.entrySet()) {
			Long key = entry.getKey();
			value = entry.getValue();
			if (key > score) {
				score = key;
				maxValue = entry.getValue();
			}
		}
		System.out.println(maxValue + " score = " + score);
	}

	static ArrayList<String> findMatchingWords(String regex) throws IOException {
		ArrayList<String> matchingWords = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader("sowpods.txt"));
		String dictionaryWord = new String();

		while ((dictionaryWord = reader.readLine()) != null) {
			if (dictionaryWord.matches(regex)) {
				matchingWords.add(dictionaryWord);
			}
		}
		reader.close();
		return matchingWords;
	}

	
	public static HashMap<Long, ArrayList<String>> computeScrabbleScores(ArrayList<String> words) throws IOException {
		HashMap<Long, ArrayList<String>> mapOfWords = new HashMap<Long, ArrayList<String>>();
		for (int i = 0; i < words.size(); i++) {
			long score = getRank(words.get(i));
			if (mapOfWords.containsKey(score)) {
				ArrayList<String> newList = mapOfWords.get(score);
				newList.add(words.get(i));
				mapOfWords.put(score, newList);
			} else {
				ArrayList<String> newList = new ArrayList<String>();
				newList.add(words.get(i));
				mapOfWords.put(score, newList);
			}
		}
		return mapOfWords;
	}

	
	static ArrayList<String> readInputFile(String fileName) throws FileNotFoundException {
		Scanner sc = new Scanner(new FileReader(fileName));
		ArrayList<String> inputWordList = new ArrayList<String>();
		while (sc.hasNextLine()) {
			String word = sc.nextLine();
			
			inputWordList.add(word.toUpperCase());
		}
		sc.close();
		return inputWordList;
	}

	
	public static int getRank(String input) {
		int[] scores = new int[] { 1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1,3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10 };
		int score = 0;
		for (int i = 0; i < input.length(); i++) {
			score += scores[input.charAt(i) - 'A'];
		}
		return score;
	}
}
