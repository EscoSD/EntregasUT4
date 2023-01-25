package com.esco;

public class Doujinshi {
	private int id;
	private String name;
	private int pages;
	private double prize;

	public Doujinshi() {}

	public Doujinshi(String name, int pages, double prize) {
		this.name = name;
		this.pages = pages;
		this.prize = prize;
	}

	public Doujinshi(int id, String name, int pages, double prize) {
		this.id = id;
		this.name = name;
		this.pages = pages;
		this.prize = prize;
	}

	public String getName() {
		return name;
	}

	public int getPages() {
		return pages;
	}

	public double getPrize() {
		return prize;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPages(int pages) {
		this.pages = pages;
	}

	public void setPrize(double prize) {
		this.prize = prize;
	}

	@Override
	public String toString() {
		return id + ".- " + name + "\nPáginas: " + pages + " \nPrecio: " + prize + "€ \n";
	}
}
