package pmf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Main {

	public static void main(String[] args) {
		tractamentXML();
	}

	public static void tractamentXML() {
		File fichero = new File("..\\Proyecto_Manager_Futbol\\xml\\config.xml");
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fichero);
			doc.getDocumentElement().normalize();
			// ruta bd y carpeta
			File carpeta_bd = new File("..\\Proyecto_Manager_Futbol\\bd");
			if (!carpeta_bd.exists())
				carpeta_bd.mkdir();
			Node nNode = doc.getElementsByTagName("ruta_bd").item(0);
			String rutaBd = nNode.getTextContent();
			// ruta equips
			nNode = doc.getElementsByTagName("ruta_equips").item(0);
			String rutaEquips = nNode.getTextContent();
			// ruta jugadors
			nNode = doc.getElementsByTagName("ruta_jugadors").item(0);
			String rutaJugadors = nNode.getTextContent();
			// ruta txt y carpeta
			File carpeta_txt = new File("..\\Proyecto_Manager_Futbol\\txt");
			if (!carpeta_txt.exists())
				carpeta_txt.mkdir();
			nNode = doc.getElementsByTagName("ruta_txt").item(0);
			String rutaTxt = nNode.getTextContent();
			// metodos
			conexion(rutaBd);
			crearTaules(rutaBd);
			insertsEquips(rutaBd, rutaEquips);
			insertsJugadors(rutaBd, rutaJugadors);
			menu(rutaBd, rutaTxt);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("..\\Proyecto_Manager_Futbol\\xml\\config.xml"));
			transformer.transform(source, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void conexion(String rutaBd) {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(rutaBd);
			System.out.println("BD Manager Futbol creada (sino existeix).");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

	public static void crearTaules(String rutaBd) {
		// Taula Equips
		String query = "CREATE TABLE IF NOT EXISTS Equips (id_equip integer PRIMARY KEY, nom_equip text(50));";
		try {
			Connection conn = DriverManager.getConnection(rutaBd);
			Statement stmt = conn.createStatement();
			stmt.execute(query);
			System.out.println("\nTaula 'Equips' creada (sino existeix).");
			conn.close();
			stmt.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		// Taula Jugadors
		query = "CREATE TABLE IF NOT EXISTS Jugadors (id_jugador integer PRIMARY KEY, nom_jugador text(50), posicio text(50), id_equip integer, nom_equip text(50), FOREIGN KEY (id_equip) REFERENCES Equips(id_equip), FOREIGN KEY (nom_equip) REFERENCES Equips(nom_equip));";
		try {
			Connection conn = DriverManager.getConnection(rutaBd);
			Statement stmt = conn.createStatement();
			stmt.execute(query);
			System.out.println("Taula 'Jugadors' creada (sino existeix).");
			conn.close();
			stmt.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void insertsEquips(String rutaBd, String rutaEquips) {
		File fichero = new File(rutaEquips);
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fichero);
			doc.getDocumentElement().normalize();
			// comprobamos si hay datos
			Connection conn = DriverManager.getConnection(rutaBd);
			PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Equips");
			ResultSet rs = pstmt.executeQuery();
			// si no hay los inserta
			if (rs.next() == false) {
				System.out.println();
				// Inserts Equips
				String query = "INSERT INTO Equips (id_equip, nom_equip) VALUES(?,?)";
				pstmt = conn.prepareStatement(query);
				// nodo = equip y lo vamos recorriendo
				NodeList nList = doc.getElementsByTagName("equip");
				for (int temp = 0; temp < nList.getLength(); temp++) {
					Node nNode = nList.item(temp);
					// obtenemos los atributos y su valor
					NamedNodeMap nodeMap = nNode.getAttributes();
					for (int i = 0; i < nodeMap.getLength(); i++) {
						Node tempNode = nodeMap.item(i);
						// insert id_equip
						int id_equip = Integer.parseInt(tempNode.getNodeValue());
						pstmt.setInt(1, id_equip);
						// nodo = nom_equip
						NodeList nList2 = doc.getElementsByTagName("nom_equip");
						Node nNode2 = nList2.item(temp);
						// insert nom_equip
						pstmt.setString(2, nNode2.getTextContent());
						pstmt.executeUpdate();
						System.out.println("Equip '" + nNode2.getTextContent() + "' insertat en la taula Equips.");
					}
				}
			}
			conn.close();
			pstmt.close();
			rs.close();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(rutaEquips));
			transformer.transform(source, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insertsJugadors(String rutaBd, String rutaJugadors) {
		File fichero = new File(rutaJugadors);
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fichero);
			doc.getDocumentElement().normalize();
			// comprobamos si hay datos
			Connection conn = DriverManager.getConnection(rutaBd);
			PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Jugadors");
			ResultSet rs = pstmt.executeQuery();
			// si no hay los inserta
			if (rs.next() == false) {
				// Inserts Jugadors
				String query = "INSERT INTO Jugadors (id_jugador, nom_jugador, posicio, id_equip, nom_equip) VALUES(?,?,?,?,?)";
				pstmt = conn.prepareStatement(query);
				// nodo = jugador y lo vamos recorriendo
				NodeList nList = doc.getElementsByTagName("jugador");
				for (int temp = 0; temp < nList.getLength(); temp++) {
					Node nNode = nList.item(temp);
					// obtenemos los atributos y su valor
					NamedNodeMap nodeMap = nNode.getAttributes();
					for (int i = 0; i < nodeMap.getLength(); i++) {
						Node tempNode = nodeMap.item(i);
						// insert id_jugador
						int id_jugador = Integer.parseInt(tempNode.getNodeValue());
						pstmt.setInt(1, id_jugador);
						// nodo = nom_jugador, insert nom_jugador
						NodeList nList2 = doc.getElementsByTagName("nom_jugador");
						Node nNode2 = nList2.item(temp);
						pstmt.setString(2, nNode2.getTextContent());
						// nodo = posicio, // insert posicio
						NodeList nList3 = doc.getElementsByTagName("posicio");
						Node nNode3 = nList3.item(temp);
						pstmt.setString(3, nNode3.getTextContent());
						// nodo = id_equip, // insert id_equip
						NodeList nList4 = doc.getElementsByTagName("id_equip");
						Node nNode4 = nList4.item(temp);
						int id_equip = Integer.parseInt(nNode4.getTextContent());
						pstmt.setInt(4, id_equip);
						// nodo = nom_equip, // insert nom_equip
						NodeList nList5 = doc.getElementsByTagName("nom_equip");
						Node nNode5 = nList5.item(temp);
						pstmt.setString(5, nNode5.getTextContent());
						pstmt.executeUpdate();
						System.out.println("Jugador '" + nNode2.getTextContent() + "' insertat en la taula Jugadors.");
					}
				}
			}
			conn.close();
			pstmt.close();
			rs.close();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(rutaJugadors));
			transformer.transform(source, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void menu(String rutaBd, String rutaTxt) {
		Scanner lector = new Scanner(System.in);
		int i = 0;
		while (i != 8) {
			System.out.println("\nMENU");
			System.out.println("   1. Mostrar equips de la lliga");
			System.out.println("   2. Mostrar informació de jugadors per equip");
			System.out.println("   3. Validar si tots els equips tenen 5 jugadors mínim");
			System.out.println("   4. Afegir jugador");
			System.out.println("   5. Esborrar jugador");
			System.out.println("   6. Intercanviar jugadors entre equips");
			System.out.println("   7. Emmagatzemar la informació dels equips en diferents fitxers de text pla");
			System.out.println("   8. Sortir");
			System.out.print("Escull una opcio: ");
			i = lector.nextInt();
			if (i > 0 && i < 9) {
				switch (i) {
				case 1:
					mostrarEquips(rutaBd);
					break;
				case 2:
					informacioJugadorsEquip(rutaBd);
					break;
				case 3:
					validarNumeroJugadors(rutaBd);
					break;
				case 4:
					afegirJugador(rutaBd);
					break;
				case 5:
					esborrarJugador(rutaBd);
					break;
				case 6:
					intercanviarJugadors(rutaBd);
					break;
				case 7:
					crearFitxers(rutaBd, rutaTxt);
					break;
				default:
					System.out.println("\nAdeu!");
					break;
				}
			} else
				System.out.println("\nError! Valor incorrecte.");
		}
	}

	public static void mostrarEquips(String rutaBd) {
		String query = "SELECT * FROM Equips";
		try {
			Connection conn = DriverManager.getConnection(rutaBd);
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			// recorremos el result set
			System.out.println("\n---Equips de la lliga---");
			while (rs.next()) {
				System.out.println(rs.getInt(1) + "\t" + rs.getString(2));
			}
			conn.close();
			stmt.close();
			rs.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void informacioJugadorsEquip(String rutaBd) {
		Scanner lector = new Scanner(System.in);
		System.out.println("\nInserta nom equip:");
		String nom_equip = lector.nextLine();
		String query = "SELECT * FROM Jugadors WHERE nom_equip = ?";
		try {
			Connection conn = DriverManager.getConnection(rutaBd);
			PreparedStatement pstmt = conn.prepareStatement(query);
			pstmt.setString(1, nom_equip);
			ResultSet rs = pstmt.executeQuery();
			// si no hay datos
			if (rs.next() == false)
				System.out.println("\nNo existeix cap equip amb aquest nom.");
			else {
				System.out.println("\n---Informacio de jugadors per equip---");
				// mientras haya datos que los muestre
				do {
					System.out.println(rs.getInt(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3) + "\t"
							+ rs.getInt(4) + "\t" + rs.getString(5));
				} while (rs.next());
			}
			conn.close();
			pstmt.close();
			rs.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void validarNumeroJugadors(String rutaBd) {
		try {
			Connection conn = DriverManager.getConnection(rutaBd);
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Equips");
			// count taula equips
			int nEquips = rs.getInt(1);
			System.out.println("\n---Validar si tots els equips tenen 5 jugadors mínim---");
			// count jugadores con el mismo id_equip
			for (int i = 1; i < nEquips + 1; i++) {
				PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM Jugadors WHERE id_equip = ?");
				pstmt.setInt(1, i);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					int nJugadors = rs.getInt(1);
					if (nJugadors < 5)
						System.out.println("Equip " + i + ": Te menys de 5 jugadors.");
					else
						System.out.println("Equip " + i + ": Validacio correcta.");
				}
			}
			conn.close();
			stmt.close();
			rs.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void afegirJugador(String rutaBd) {
		Scanner lectorInt = new Scanner(System.in);
		Scanner lectorString = new Scanner(System.in);
		// datos
		System.out.println("\nInserta id jugador:");
		int id_jugador = lectorInt.nextInt();
		System.out.println("Inserta nom jugador:");
		String nom_jugador = lectorString.nextLine();
		System.out.println("Inserta posicio:");
		String posicio = lectorString.nextLine();
		System.out.println("Inserta id equip:");
		int id_equip = lectorInt.nextInt();
		String query = "INSERT INTO Jugadors (id_jugador, nom_jugador, posicio, id_equip, nom_equip) VALUES(?,?,?,?,?)";
		try {
			Connection conn = DriverManager.getConnection(rutaBd);
			PreparedStatement pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, id_jugador);
			pstmt.setString(2, nom_jugador);
			pstmt.setString(3, posicio);
			pstmt.setInt(4, id_equip);
			// select equipo pidiendo id_equip
			PreparedStatement pstmt2 = conn.prepareStatement("SELECT nom_equip FROM Equips WHERE id_equip = ?");
			pstmt2.setInt(1, id_equip);
			ResultSet rs = pstmt2.executeQuery();
			String nom_equip = rs.getString(1);
			pstmt.setString(5, nom_equip);
			pstmt.executeUpdate();
			System.out.println("\nJugador '" + nom_jugador + "' insertat en la taula Jugadors (" + nom_equip + ").");
			conn.close();
			pstmt.close();
			pstmt2.close();
			rs.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void esborrarJugador(String rutaBd) {
		Scanner lector = new Scanner(System.in);
		System.out.println("\nInserta id jugador:");
		int id_jugador = lector.nextInt();
		try {
			Connection conn = DriverManager.getConnection(rutaBd);
			PreparedStatement pstmt = conn.prepareStatement("SELECT nom_jugador FROM Jugadors WHERE id_jugador = ?");
			pstmt.setInt(1, id_jugador);
			ResultSet rs = pstmt.executeQuery();
			// si no hay datos
			if (rs.next() == false)
				System.out.println("\nNo existeix cap jugador amb aquest id.");
			else {
				String nom_jugador = rs.getString(1);
				pstmt = conn.prepareStatement("DELETE FROM Jugadors WHERE id_jugador = ?");
				pstmt.setInt(1, id_jugador);
				pstmt.executeUpdate();
				System.out.println("\nJugador '" + nom_jugador + "' esborrat correctament.");
			}
			conn.close();
			pstmt.close();
			rs.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void intercanviarJugadors(String rutaBd) {
		Scanner lector = new Scanner(System.in);
		// jugador A
		System.out.println("\nInserta id del primer jugador:");
		int id_jugadorA = lector.nextInt();
		String query = "SELECT id_equip, nom_equip FROM Jugadors WHERE id_jugador = ?";
		try {
			Connection conn = DriverManager.getConnection(rutaBd);
			PreparedStatement pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, id_jugadorA);
			ResultSet rs = pstmt.executeQuery();
			// si no hay datos
			if (rs.next() == false)
				System.out.println("\nNo existeix cap jugador amb aquest id.");
			else {
				// jugador B
				System.out.println("Inserta id del segon jugador:");
				int id_jugadorB = lector.nextInt();
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, id_jugadorB);
				ResultSet rs2 = pstmt.executeQuery();
				// si no hay datos
				if (rs2.next() == false)
					System.out.println("\nNo existeix cap jugador amb aquest id.");
				else {
					// si escribe el mismo id
					if (id_jugadorA == id_jugadorB) {
						System.out.println("\nHas escrit el mateix id dos cops.");
					} else if (rs.getString(2).equals(rs2.getString(2))) {
						// si los jugadores tienen el mismo id_equip
						System.out.println("\nEls dos jugadors pertanyen al mateix equip.");
					} else {
						String query2 = "UPDATE Jugadors SET id_equip = ?, nom_equip = ? WHERE id_jugador = ?";
						PreparedStatement pstmt2 = conn.prepareStatement(query2);
						// update jugador A
						pstmt2.setInt(1, rs.getInt(1));
						pstmt2.setString(2, rs.getString(2));
						pstmt2.setInt(3, id_jugadorB);
						pstmt2.executeUpdate();
						// update jugador B
						pstmt2.setInt(1, rs2.getInt(1));
						pstmt2.setString(2, rs2.getString(2));
						pstmt2.setInt(3, id_jugadorA);
						pstmt2.executeUpdate();
						System.out.println("\nJugadors intercanviats correctament.");
						pstmt2.close();
						rs2.close();
					}
				}
			}
			conn.close();
			pstmt.close();
			rs.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void crearFitxers(String rutaBd, String rutaTxt) {
		try {
			Connection conn = DriverManager.getConnection(rutaBd);
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Equips");
			// count taula equips
			int nEquips = rs.getInt(1);
			// recorremos el for por equipo cada vez
			for (int i = 1; i < nEquips + 1; i++) {
				PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Jugadors WHERE id_equip = ?");
				pstmt.setInt(1, i);
				rs = pstmt.executeQuery();
				// creamos el fichero y agregamos contenido sin sobreescribir
				File fichero = new File(rutaTxt + i + ".txt");
				BufferedWriter bw;
				// si el fichero existe que lo sobreescriba
				if (fichero.exists())
					bw = new BufferedWriter(new FileWriter(fichero));
				else
					bw = new BufferedWriter(new FileWriter((fichero), true));
				while (rs.next()) {
					bw.write(rs.getInt(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3) + "\t" + rs.getInt(4) + "\t"
							+ rs.getString(5));
					bw.newLine();
				}
				pstmt.close();
				bw.close();
			}
			System.out.println("\nFitxers txt creats correctament (1 per equip).");
			conn.close();
			stmt.close();
			rs.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}