package io.github.sspanak.tt9.ime.modes;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class ModeABC extends InputMode {
	private final SettingsStore settings;

	public int getId() { return MODE_ABC; }

	private boolean shouldSelectNextLetter = false;

	ModeABC(SettingsStore settings, Language lang) {
		this.settings = settings;
		changeLanguage(lang);
	}

	@Override
	public boolean onBackspace() {
		reset();
		return false;
	}

	@Override
	public boolean onNumber(int number, boolean hold, int repeat) {
		if (hold) {
			reset();
			autoAcceptTimeout = 0;
			digitSequence = String.valueOf(number);
			shouldSelectNextLetter = false;
			suggestions.add(language.getKeyNumber(number));
		} else if (repeat > 0) {
			autoAcceptTimeout = settings.getAbcAutoAcceptTimeout();
			shouldSelectNextLetter = true;
		} else {
			reset();
			autoAcceptTimeout = settings.getAbcAutoAcceptTimeout();
			digitSequence = String.valueOf(number);
			shouldSelectNextLetter = false;
			suggestions.addAll(language.getKeyCharacters(number));
			suggestions.add(language.getKeyNumber(number));
		}

		return true;
	}

	@Override
	protected String adjustSuggestionTextCase(String word, int newTextCase) {
		return newTextCase == CASE_UPPER ? word.toUpperCase(language.getLocale()) : word.toLowerCase(language.getLocale());
	}

	private void refreshSuggestions() {
		if (digitSequence.isEmpty()) {
			suggestions.clear();
		} else {
			onNumber(digitSequence.charAt(0) - '0', false, 0);
		}
	}

	@Override
	protected boolean nextSpecialCharacters() {
		if (digitSequence.equals(NaturalLanguage.SPECIAL_CHARS_KEY) && super.nextSpecialCharacters()) {
			suggestions.add(language.getKeyNumber(digitSequence.charAt(0) - '0'));
			return true;
		}

		return false;
	}

	@Override
	public void changeLanguage(Language language) {
		super.changeLanguage(language);

		allowedTextCases.clear();
		allowedTextCases.add(CASE_LOWER);
		if (language.hasUpperCase()) {
			allowedTextCases.add(CASE_UPPER);
		}

		refreshSuggestions();
		shouldSelectNextLetter = true; // do not accept any previous suggestions after loading the new ones
	}

	@Override public void onAcceptSuggestion(@NonNull String word) { reset(); }
	@Override public boolean shouldAcceptPreviousSuggestion() { return !shouldSelectNextLetter; }
	@Override public boolean shouldSelectNextSuggestion() { return shouldSelectNextLetter; }

	@Override
	public void reset() {
		super.reset();
		digitSequence = "";
		shouldSelectNextLetter = false;
	}

	@NonNull
	@Override
	public String toString() {
		if (language == null) {
			return textCase == CASE_LOWER ? "abc" : "ABC";
		}

		String langCode = "";
		if (LanguageKind.isLatinBased(language) || LanguageKind.isCyrillic(language)) {
			// There are many languages written using the same alphabet, so if the user has enabled multiple,
			// make it clear which one is it, by appending the country code to "ABC" or "АБВ".
			langCode = language.getLocale().getCountry();
			langCode = langCode.isEmpty() ? language.getLocale().getLanguage() : langCode;
			langCode = langCode.isEmpty() ? language.getName() : langCode;
			langCode = " / " + langCode;
		}
		String modeString = language.getAbcString() + langCode.toUpperCase();

		return (textCase == CASE_LOWER) ? modeString.toLowerCase(language.getLocale()) : modeString.toUpperCase(language.getLocale());
	}
}
