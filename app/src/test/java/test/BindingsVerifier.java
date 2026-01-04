package test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import elite.intel.ai.hands.*;
//import elite.intel.ai.hands.KeyBindingsParser.KeyBinding;
import elite.intel.ai.brain.handlers.commands.Bindings.GameCommand;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
//import java.lang.reflect.Field;
//import java.util.Map;

public class BindingsVerifier {
	private static final Logger log = LogManager.getLogger(BindingsVerifier.class);
	private static BindingsVerifier instance;
	private Path bindingsDir = Path.of("/home/andreas/.steam/steam/steamapps/compatdata/359320/pfx/drive_c/users/steamuser/AppData/Local/Frontier Developments/Elite Dangerous/Options/Bindings/");
	
	private BindingsVerifier() {
    	// Private constructor for singleton
	}

	public static synchronized BindingsVerifier getInstance() {
		if (instance == null) {
			instance = new BindingsVerifier();
		}
		return instance;
	}

	private File getLatestBinings() throws Exception {
		Path latestFilePath = Files.list(this.bindingsDir)
		.filter( fi -> fi.toString().endsWith(".binds"))
		.max(Comparator.comparingLong(fi -> fi.toFile().lastModified()))
		.orElseThrow(() -> new Exception("No .binds file found in " + bindingsDir));

		File latestFile = latestFilePath.toFile();
		log.info("Selected latest bindings file: {}", latestFile.getName());

		return latestFile;
	}

	public NodeList getBindings() throws Exception {
		File bindingsFile = getLatestBinings();
		// XML basic builder
		// https://www.geeksforgeeks.org/java/read-and-write-xml-files-in-java/
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		// EI mirror
		Document bindingDoc = builder.parse(bindingsFile);
		bindingDoc.getDocumentElement().normalize();
		
		// Create Tutorial 
		/*
		Document document = builder.newDocument();
		Element root = document.createElement("library");
		document.appendChild(root);
		*/

		// Read Tutorial
		NodeList nodes = bindingDoc.getDocumentElement().getChildNodes();
		return nodes;
        /*
          	<KeyboardLayout>en-US</KeyboardLayout>
          	<BlockMouseDecay>
				<Primary Device="{NoDevice}" Key="" />
				<Secondary Device="{NoDevice}" Key="" />
				<ToggleOn Value="0" />
			</BlockMouseDecay>
			<SetSpeed100>
				<Primary Device="Keyboard" Key="Key_Numpad_0" />
				<Secondary Device="{NoDevice}" Key="" />
			</SetSpeed100>
        */
		/*
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);			

			if (node instanceof Element) {
				Element element = (Element) node;
				String tag = element.getTagName();
				String display = "Name: " +tag;
				
				NodeList primaryList = element.getElementsByTagName("Primary");
                NodeList secondaryList = element.getElementsByTagName("Secondary");
                NodeList toggle = element.getElementsByTagName("ToggleOn");

				if (primaryList.getLength() > 0) {
					Element el = (Element) primaryList.item(0);
					String key = el.hasAttribute("Key") 
						&& !el.getAttribute("Key").isEmpty() 
						? el.getAttribute("Key") 
						: "-";
					String device = el.getAttribute("Device");

					if ("{NoDevice}".equals(device)) {
						display += ", Unbound" ;
					}
					else {
						display += ", Primary: ["+device+"] = "+key;
					}
				}
				if (secondaryList.getLength() > 0) {
					Element el = (Element) secondaryList.item(0);
					String key = el.hasAttribute("Key") 
						&& !el.getAttribute("Key").isEmpty() 
						? el.getAttribute("Key") 
						: "-";
					String device = el.getAttribute("Device");

					if ("{NoDevice}".equals(device)) {
						display += ", Unbound" ;
					}
					else {
						display += ", Secondary: ["+device+"] = "+key;
					}
				}
				if (toggle.getLength() > 0) {
					Element el = (Element) toggle.item(0);
					String key = el.hasAttribute("Key") 
						&& !el.getAttribute("Key").isEmpty() 
						? el.getAttribute("Key") 
						: "-";
						String device = el.getAttribute("Device");

						display += ", Hold: ["+device+"] = "+key;
				}
				// If the element has now childrens
				if (primaryList.getLength() == 0
					&& secondaryList.getLength() == 0
					&& toggle.getLength() == 0) {

					String value = element.hasAttribute("Value") 
						&& !element.getAttribute("Value").isEmpty() 
						? element.getAttribute("Value") 
						: element.getTextContent().trim();
					display += ", Value: " + value;
				}

				System.out.println(display);
			}
		}
		*/
	}
	

	public void testEnums() throws Exception {
		NodeList nodes = getBindings();
		// Match the binding
		for (GameCommand binding : GameCommand.values()) {
			String requiredBinding = binding.getGameBinding();
			String bindingValue = requiredBinding;

			// find the associated KeyBind
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node instanceof Element) {
					Element element = (Element) node;
					String tag = element.getTagName();

					if(requiredBinding.equals(tag)) {
						NodeList primaryList = element.getElementsByTagName("Primary");
						NodeList secondaryList = element.getElementsByTagName("Secondary");
						boolean boolPrimaryIsSet = false;
						boolean boolSecondaryIsSet = false;

						if (primaryList.getLength() > 0) {
							Element primary = (Element) primaryList.item(0);
							if ("Keyboard".equals(primary.getAttribute("Device"))
								&& primary.hasAttribute("Key") 
								&& !primary.getAttribute("Key").isEmpty()) {
								String key = primary.getAttribute("Key");
								bindingValue += ", PrimaryKey = " + key;
								boolPrimaryIsSet = true;
							}
						}
						if (secondaryList.getLength() > 0) {
							Element secondary = (Element) secondaryList.item(0);
							if ("Keyboard".equals(secondary.getAttribute("Device"))
								&& secondary.hasAttribute("Key") 
								&& !secondary.getAttribute("Key").isEmpty()) {
								String key = secondary.getAttribute("Key");
								bindingValue += ", SecondaryKey = " + key;
								boolSecondaryIsSet = true;
							}
						}

						if (!boolSecondaryIsSet && !boolPrimaryIsSet) {
							bindingValue += " [Error]: No bindings for this action";
						}
					}
				}

			}
            System.out.println(bindingValue);
		}
	}
	/*
	// AI example slop

	private static String format(Object kb) throws ReflectiveOperationException {
		Class <?> c = kb.getClass();

		Field fKey = c.getDeclearedField("key");
		Field fModifiers = c.getDeclearedField("modifiers");
		Field fHold = c.getDeclearedField("hold");

		fKey.setAccesible(true);
		fModifiers.setAccesible(true);
		fHold.setAccesible(true);

		String key = (String) fKey.get(kb);
		String modifiers = (String[]) fModifiers.get(kb);
		boolean hold = (boolean) fHold.get(kb);

		StringBuilder sb = new StringBuilder();
		if (mods != null) {
			for (String m : mods) sb.append(m).append("=");
		}
		sb.append(key);
		if(hold) sb.append(" (hold)");
		return sb.toString();
	}

	public static void print(Map<String, ?> map) {
		map.forEach((action, kb) -> {
			try {
				System.out.println(action + " = " + format(kb));
			}
			catch (Exception e) {
                System.err.println("Could not decode binding for " + action);
            }
		});
	}*/
}