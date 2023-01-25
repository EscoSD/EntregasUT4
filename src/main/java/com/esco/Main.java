package com.esco;

import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import org.bson.Document;

import java.util.*;

public class Main {
	static MongoDatabase database;
	static MongoCollection<Document> collection;
	static Scanner scan;
	static int id;

	public static void main(String[] args) {
		try (MongoClient client = new MongoClient("192.168.1.21")) {

			database = client.getDatabase("EntregasDatabase");
			collection = database.getCollection("doujinshis");

			id = databaseMaxId();
			scan = new Scanner(System.in);

			menu: while (true) {
				System.out.print("\n1.- Añadir Doujinshi\n2.- Actualizar Doujinshi\n3.- Eliminar Doujinshi\n4.- Listar Doujinshis\n5.- Extras\n0.- Salir\n.-  ");

				switch (getInt()) {
					case 1 -> create();
					case 2 -> update();
					case 3 -> delete();
					case 4 -> list();
					case 5 -> query();
					case 0 -> { break menu; }
					default -> System.out.println("Introduce un número válido");
				}
			}
		}
	}

	static void create() {
		System.out.print("\nNúmero de Doujinshis a crear.- ");
		int inserts = getInt();

		if (inserts == 1)
			collection.insertOne(newDocument());

		else if (inserts > 1) {
			List<Document> list = new ArrayList<>();

			for (int i = 0; i < inserts; i++) {
				System.out.printf("\nDoujinshi %d: \n", i + 1);
				list.add(newDocument());
			}
			collection.insertMany(list);
		} else {
			System.out.println("Inserta un número superior o igual a uno.");
			create();
		}
	}

	private static Document newDocument() {
		String name;
		int pages;
		double prize;

		do {
			System.out.print("Nombre: ");
			name = scan.nextLine();
		} while(name.isEmpty() || name.isBlank());
		do {
			System.out.print("Número de páginas: ");
			pages = getInt();
		} while(pages <= 0);
		do {
			System.out.print("Precio: ");
			prize = getDouble();
		} while(prize <= 0);

		return new Document("_id", ++id)
				.append("nombre", name)
				.append("páginas", pages)
				.append("precio", prize);
	}

	static void update() {
		System.out.print("\nID del Doujinshi a actualizar: ");
		int updateID = getInt();

		FindIterable<Document> iterable = collection.find(new Document("_id", updateID));
		Iterator<Document> iterator = iterable.iterator();

		if (!iterator.hasNext())
			System.out.println("No se ha encontrado el Doujinshi con ID " + updateID);

		else {
			Document updated;
			Document base = iterator.next();
			Doujinshi doujinshi = new Doujinshi(
					base.getInteger("_id"),
					base.getString("nombre"),
					base.getInteger("páginas"),
					base.getDouble("precio")
			);

			System.out.println("\n" + doujinshi + "\nElige el campo a actualizar: nombre, páginas, precio.");

			switch (scan.nextLine()) {
				case "nombre" -> {
					String newName;
					do {
						System.out.print("Inserta un nuevo nombre: ");
						newName = scan.nextLine();
					} while(newName.isEmpty() || newName.isBlank());

					updated = new Document("$set", new Document("nombre", newName));
				}
				case "páginas" -> {
					int newPages;
					do {
						System.out.print("Inserta un nuevo número de páginas: ");
						newPages = getInt();
					} while(newPages <= 0);
					updated = new Document("$set", new Document("páginas", newPages));
				}
				case "precio" -> {
					double newPrize;
					do {
						System.out.print("Inserta un nuevo precio: ");
						newPrize = getDouble();
					} while(newPrize <= 0);
					updated = new Document("$set", new Document("precio", newPrize));
				}
				default -> {
					System.out.println("Saliendo.");
					return;
				}
			}
			collection.updateOne(base, updated);
		}
	}

	static void delete() {
		System.out.print("\nID del Doujinshi a eliminar: ");

		int deleteID = getInt();

		// Si el número de eliminados es 0, se imprime el primer String, si no, se imprime el segundo
		System.out.println(
				collection.deleteOne(new Document("_id", deleteID)).getDeletedCount() == 0 ?
						"No se ha encontrado el Doujinshi con ID " + deleteID : "Eliminado con éxito"
		);
	}

	static void list() {
		List<Doujinshi> doujins = new ArrayList<>();
		FindIterable<Document> iterable = collection.find();
		Iterator<Document> iterator = iterable.iterator();

		if (iterator.hasNext()) {
			iterateAndShow(doujins, iterable);
		}

		else
			System.out.println("\nNo hay Doujinshis a mostrar.");
	}

	static void query() {

		AggregateIterable<Document> aggregateIterable;

		System.out.print("""

				Consultas
				1.- Consultar el precio más alto y el más bajo.
				2.- Mostrar los Doujinshis con más de 40 páginas.
				3.- Sumar 5€ al precio de todos los Doujinshis.
				.-\s""");

		int option = scan.nextInt();

		switch (option) {
			case 1 -> {
				aggregateIterable = collection.aggregate(
						List.of(
								Aggregates.group(
										null,
										Accumulators.min("MinPrecio", "$precio"),
										Accumulators.max("MaxPrecio", "$precio")
								)
						)
				);
				System.out.println();
				for (Document document: aggregateIterable) {
					System.out.println("Precio más bajo: " + document.get("MinPrecio"));
					System.out.println("Precio más alto: " + document.get("MaxPrecio"));
				}
			}

			case 2 -> {
				List<Doujinshi> doujins = new ArrayList<>();
				Document document = new Document("páginas", new Document("$gt", 30));
				FindIterable<Document> findIterable =  collection.find(document);

				if (findIterable.iterator().hasNext()) {
					iterateAndShow(doujins, findIterable);
				}
				else
					System.out.println("No hay Doujinshis que cumplan esa condición.");


			}
			case 3 -> {
				Document filter = new Document();
				Document updated = new Document("$inc", new Document("precio", 5));
				collection.updateMany(filter, updated);
			}
			default -> System.out.println();
		}
	}

	private static void iterateAndShow(List<Doujinshi> doujins, FindIterable<Document> findIterable) {
		for (Document doc : findIterable) {
			Doujinshi doujin = new Doujinshi(
					doc.getInteger("_id"),
					doc.getString("nombre"),
					doc.getInteger("páginas"),
					doc.getDouble("precio"));
			doujins.add(doujin);
		}

		System.out.println();
		for (Doujinshi d : doujins)
			System.out.println(d);
	}

	static int databaseMaxId() {
		int num;

		try {
			num = (int) collection.find().sort(new Document("_id", -1))
					.limit(1).iterator().next().get("_id");
		} catch (NoSuchElementException e) {
			num = 0;
		}
		return num;
	}

	static int getInt() {
		int num;

		while (true)
			try {
				num = scan.nextInt();
				if (num < 0)
					throw new InputMismatchException();
				scan.nextLine();
				break;
			} catch (InputMismatchException e) {
				scan.nextLine();
				System.out.print("Inserta un número válido.- ");
			}
		return num;
	}

	static double getDouble() {
		double num;

		while (true)
			try {
				num = scan.nextDouble();
				if (num < 0)
					throw new InputMismatchException();
				scan.nextLine();
				break;
			} catch (InputMismatchException e) {
				scan.nextLine();
				System.out.print("Inserta un número válido.- ");
			}
		return num;
	}
}
