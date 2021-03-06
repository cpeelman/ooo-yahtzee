package domain;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import domain.Category.LowerSectionCategory;
import domain.Category.SpecialCategory;
import domain.Category.UpperSectionCategory;

public class CategoryScore {
	private ArrayList<Dice> pickedDice;
	private Category category;
	private int points, bonusYahtzee;
	private boolean playing, picked;

	public CategoryScore(Category category) {
		this.category = category;
		if (category instanceof SpecialCategory || category.equals(LowerSectionCategory.BONUS_YAHTZEE)) {
			this.points = 0;
		} else {
			this.points = -1;
		}
		this.bonusYahtzee = -1;
		this.playing = false;
		this.picked = false;
	}

	public CategoryScore(Category category, ArrayList<Dice> pickedDice) {
		this.category = category;
		setDice(pickedDice);
	}

	public void setDice(ArrayList<Dice> pickedDice) {
		this.pickedDice = pickedDice;
		this.points = calculatePoints();
	}

	public int getPoints() {
		return this.points;
	}

	private int calculatePoints() {
		if (isLegitCategory()) {
			if (this.category instanceof UpperSectionCategory) {
				return upperSectionPoints();
			} else if (this.category instanceof LowerSectionCategory) {
				return lowerSectionPoints();
			}
		} else if (category.equals(LowerSectionCategory.BONUS_YAHTZEE)) {
			return this.points;
		}
		return 0;
	}

	public int upperSectionPoints() {
		UpperSectionCategory upperSectionCategory = (UpperSectionCategory) this.category;
		switch (upperSectionCategory) {
		case ACES:
			return getFrequencyOfValue(1);
		case TWOS:
			return getFrequencyOfValue(2) * 2;
		case THREES:
			return getFrequencyOfValue(3) * 3;
		case FOURS:
			return getFrequencyOfValue(4) * 4;
		case FIVES:
			return getFrequencyOfValue(5) * 5;
		case SIXES:
			return getFrequencyOfValue(6) * 6;
		}
		return 0;
	}

	public int lowerSectionPoints() {
		LowerSectionCategory lowerSectionCategory = (LowerSectionCategory) this.category;
		switch (lowerSectionCategory) {
		case THREE_OF_A_KIND:
		case FOUR_OF_A_KIND:
		case CHANCE:
			return getSumOfDice();
		case FULL_HOUSE:
			return 25;
		case SMALL_STRAIGHT:
			return 30;
		case LARGE_STRAIGHT:
			return 40;
		case YAHTZEE:
			return 50;
		case BONUS_YAHTZEE:
			return (bonusYahtzee + 1) * 100;
		}
		return 0;
	}

	public int getFrequencyOfValue(int value) {
		int frequency = 0;
		for (Dice dice : pickedDice) {
			if (dice.getNumber() == value) {
				frequency++;
			}
		}
		return frequency;
	}

	public int getSumOfDice() {
		int sum = 0;
		for (Dice dice : pickedDice) {
			sum += dice.getNumber();
		}
		return sum;
	}

	public boolean isLegitCategory() {
		if (this.category instanceof UpperSectionCategory || this.category instanceof SpecialCategory) {
			return true;
		} else if (this.category instanceof LowerSectionCategory) {
			LowerSectionCategory lowerSectionCategory = (LowerSectionCategory) this.category;
			switch (lowerSectionCategory) {
			case THREE_OF_A_KIND:
				return correctFrequencyOfDice(3) ? true : false;
			case FOUR_OF_A_KIND:
				return correctFrequencyOfDice(4) ? true : false;
			case FULL_HOUSE:
				return fullHouse();
			case SMALL_STRAIGHT:
				return straight(pickedDice) >= 4 ? true : false;
			case LARGE_STRAIGHT:
				return straight(pickedDice) == 5 ? true : false;
			case YAHTZEE:
				return correctFrequencyOfDice(5) ? true : false;
			case CHANCE:
				return true;
			case BONUS_YAHTZEE:
				return correctFrequencyOfDice(5) ? true : false;
			default:
				break;
			}
			return false;
		}
		return false;
	}

	public boolean correctFrequencyOfDice(int frequency) {
		for (Dice dice : pickedDice) {
			if (getFrequencyOfValue(dice.getNumber()) >= frequency) {
				return true;
			}
		}
		return false;
	}

	public boolean fullHouse() {
		int three = 0;
		int two = 0;
		for (Dice dice : pickedDice) {
			if (getFrequencyOfValue(dice.getNumber()) == 3) {
				three = dice.getNumber();
			} else if (getFrequencyOfValue(dice.getNumber()) == 2) {
				two = dice.getNumber();
			}
		}
		return (three != 0 && two != 0);
	}

	public static int straight(ArrayList<Dice> setOfDice) {
		SortedSet<Integer> values = new TreeSet<>();
		for (Dice dice : setOfDice) {
			values.add(dice.getNumber());
		}
		int straightLevel = 1;
		ArrayList<Integer> valuesSorted = new ArrayList<Integer>(values);
		for (int i = 0; i < valuesSorted.size() - 1; i++) {
			if (valuesSorted.get(i) == valuesSorted.get(i + 1) - 1) {
				straightLevel += 1;
			} else {
				straightLevel = 1;
			}
		}
		return straightLevel;
	}

	public Category getCategory() {
		return this.category;
	}

	public static CategoryScore getEmptyCategoryScore(Category category, int initialScore) {
		CategoryScore categoryScore = new CategoryScore(category);
		categoryScore.setPoints(initialScore);
		return categoryScore;
	}

	private void setPoints(int points) {
		this.points = points;
	}

	public ArrayList<Dice> getDice() {
		return this.pickedDice;
	}

	public CategoryScore updateTotals(CategoryScore categoryScore) {
		if (!this.getCategory().equals(LowerSectionCategory.BONUS_YAHTZEE)) {
			categoryScore.setPoints(categoryScore.getPoints() + this.points);
		} else {
			categoryScore.setPoints(categoryScore.getPoints() + 100);
		}
		return categoryScore;
	}

	public CategoryScore addBonus(CategoryScore categoryScore) {
		if (categoryScore.getCategory().equals(SpecialCategory.UPPER_SECTION_BONUS)) {
			categoryScore.setPoints(35);
		} else if (categoryScore.getCategory().equals(SpecialCategory.UPPER_SECTION_TOTAL)) {
			categoryScore.setPoints(categoryScore.getPoints() + 35);
		} else if (categoryScore.getCategory().equals(SpecialCategory.GRAND_TOTAL)) {
			categoryScore.setPoints(categoryScore.getPoints() + 35);
		}
		return categoryScore;

	}

	public void reset() {
		setPoints(-1);
	}

	public boolean isPicked() {
		return picked;
	}

	public void setPicked(boolean picked) {
		if (this.getCategory().equals(LowerSectionCategory.BONUS_YAHTZEE)) {
			updateBonusYahtzee();
		} else {
			this.picked = picked;
		}
	}

	public void updateBonusYahtzee() {
		this.bonusYahtzee += 1;
	}

	public boolean isPlaying() {
		return playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

}