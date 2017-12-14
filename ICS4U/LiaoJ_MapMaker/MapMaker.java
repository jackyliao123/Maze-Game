// Jacky Liao
// December 12, 2017
// Maze Game
// ICS4U Ms.Strelkovska

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.function.Consumer;

public class MapMaker {

	static {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
		}
	}

	public JFrame frame;
	public GamePanel panel;
	public JPanel properties;
	public JButton playButton;

	public MapMaker() {
		frame = new JFrame("Map Maker");
		panel = new GamePanel(this);
		panel.setPreferredSize(new Dimension(960, 540));
		frame.add(panel);

		// Menu bars
		JMenuBar menuBar = new JMenuBar();
		JMenu menuFile = new JMenu("File");
		menuBar.add(menuFile);

		JMenuItem itemOpen = new JMenuItem("Open");
		JMenuItem itemSave = new JMenuItem("Save");
		JMenuItem itemSaveAs = new JMenuItem("Save As");
		itemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		itemSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		itemOpen.addActionListener(e -> open());
		itemSave.addActionListener(e -> save(false));
		itemSaveAs.addActionListener(e -> save(true));
		menuFile.add(itemOpen);
		menuFile.add(itemSave);
		menuFile.add(itemSaveAs);


		JMenu menuEdit = new JMenu("Edit");
		menuBar.add(menuEdit);

		JMenuItem itemUndo = new JMenuItem("Undo");
		JMenuItem itemRedo = new JMenuItem("Redo");
		itemUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		itemRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
		itemUndo.addActionListener(e -> panel.undo());
		itemRedo.addActionListener(e -> panel.redo());
		menuEdit.add(itemUndo);
		menuEdit.add(itemRedo);

		frame.setJMenuBar(menuBar);

		// Right control panel
		JPanel panelControl = new JPanel();

		panelControl.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;

		JPanel panelNewObject = new JPanel();
		panelNewObject.setLayout(new BorderLayout());

		JTabbedPane tabTools = new JTabbedPane();

		// Generate a panel for each one of the objects
		for(int i = 0; i < GamePanel.classes.length; ++i) {
			JPanel panel = new JPanel();
			ObjProperty property = generatePanel(panel, GamePanel.classes[i], GamePanel.defaultObject[i]);
			tabTools.addTab(property.name(), panel);
		}

		// When tab changes, update the tool as well
		tabTools.addChangeListener(e -> {
			if(panel.play) {
				panel.lastEditMode = tabTools.getSelectedIndex();
			} else {
				panel.editMode = tabTools.getSelectedIndex();
			}
		});

		panelNewObject.add(tabTools);

		constraints.gridy++;
		panelControl.add(panelNewObject, constraints);

		// Global world properties
		properties = new JPanel();
		properties.setBorder(BorderFactory.createTitledBorder("Properties"));
		generatePanel(properties, DummyProperties.class, panel.properties);
		constraints.gridy++;
		panelControl.add(properties, constraints);

		// Play button
		playButton = new JButton("Play");
		playButton.addActionListener(e -> {
			if(panel.play) {
				panel.enterEdit();
			} else {
				panel.enterPlay();
			}
		});

		playButton.setPreferredSize(new Dimension(220, 50));

		constraints.gridy++;
		panelControl.add(playButton, constraints);

		// Filler so that GridBagLayout is happy
		JPanel filler = new JPanel();
		constraints.gridy++;
		constraints.weighty = 1.0;
		panelControl.add(filler, constraints);

		// Scroll panel for the right panel
		JScrollPane scrollPane = new JScrollPane(panelControl);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		frame.add(scrollPane, BorderLayout.EAST);

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	// Generate a panel based on the annotations on the class
	private ObjProperty generatePanel(JPanel panel, Class clazz, Object object) {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints cons = new GridBagConstraints();
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.insets = new Insets(5, 0, 5, 0);
		ObjProperty property = (ObjProperty)clazz.getAnnotation(ObjProperty.class);
		// Go through each one of the field
		for(Field field : clazz.getDeclaredFields()) {
			Annotation prop = field.getAnnotation(GUIProperty.class);
			if(prop != null) {
				// If the field has the GUIProperty annotation
				GUIProperty guiProp = (GUIProperty) prop;
				JPanel pp = new JPanel();
				pp.setLayout(new BorderLayout());
				pp.setBorder(BorderFactory.createTitledBorder(guiProp.name()));
				++cons.gridy;
				try {
					// What type is it?
					switch(guiProp.type()) {
						case COLOR:
							ColorChooser chooser = new ColorChooser();
							chooser.clr = field.getInt(object);
							chooser.setActionListener(e -> {
								try {
									field.setInt(object, chooser.clr);
								} catch(IllegalAccessException ex) {
									ex.printStackTrace();
								}
							});
							pp.add(chooser);
							break;
						case STRING:
							JTextField tf = new JTextField();
							tf.setText((String) field.get(object));
							tf.setPreferredSize(new Dimension(220, 30));
							tf.getDocument().addDocumentListener(new DocumentListener() {
								public void update() {
									try {
										field.set(object, tf.getText());
									} catch(IllegalAccessException ex) {
										ex.printStackTrace();
									}
								}

								public void insertUpdate(DocumentEvent e) {
									update();
								}

								public void removeUpdate(DocumentEvent e) {
									update();
								}

								public void changedUpdate(DocumentEvent e) {
									update();
								}
							});
							pp.add(tf);
							break;
						case NUMERIC:
							boolean floating = field.getType() == double.class;
							JSlider slider;
							JSpinner spinner = new JSpinner();
							spinner.setPreferredSize(new Dimension(220, 30));
							Consumer<Object> update = v -> {
								try {
									field.set(object, floating ? v : (int) (double) v);
								} catch(IllegalAccessException ex) {
									ex.printStackTrace();
								}
							};
							if(floating) {
								// Field has type double
								double l = guiProp.rangeL();
								double h = guiProp.rangeH();
								int mapped = (int) ((field.getDouble(object) - l) / (h - l) * 2147483647);
								slider = new JSlider(0, 2147483647, mapped);
								spinner.setModel(new SpinnerNumberModel(field.getDouble(object), guiProp.min(), guiProp.max(), 0.01));
								slider.addChangeListener(e -> {
									spinner.setValue(slider.getValue() / 2147483647.0 * (h - l) + l);
									update.accept(spinner.getValue());
								});
								spinner.addChangeListener(e -> {
									slider.setValue((int) (((double) spinner.getValue() - l) / (h - l) * 2147483647));
									update.accept(spinner.getValue());
								});
							} else {
								// Field has type int
								slider = new JSlider((int) guiProp.rangeL(), (int) guiProp.rangeH(), field.getInt(object));
								spinner.setModel(new SpinnerNumberModel(field.getInt(object), guiProp.min(), guiProp.max(), 1));
								slider.addChangeListener(e -> {
									spinner.setValue((double) slider.getValue());
									update.accept(spinner.getValue());
								});
								spinner.addChangeListener(e -> {
									slider.setValue((int) (double) spinner.getValue());
									update.accept(spinner.getValue());
								});
							}

							pp.add(slider);
							pp.add(spinner, "South");
							break;
						case CHECK:
							JCheckBox cb = new JCheckBox(guiProp.name());
							cb.setSelected(field.getBoolean(object));
							cb.addItemListener(e -> {
								try {
									field.setBoolean(object, cb.isSelected());
								} catch(IllegalAccessException ex) {
									ex.printStackTrace();
								}
							});
							pp.add(cb);
							break;
						case ANGLE:
							DirectionChooser dir = new DirectionChooser();
							dir.ang = field.getDouble(object);
							dir.setActionListener(e -> {
								try {
									field.setDouble(object, dir.ang);
								} catch(IllegalAccessException ex) {
									ex.printStackTrace();
								}
							});
							pp.add(dir);
							break;
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
				panel.add(pp, cons);
			}
		}
		cons.gridy++;
		cons.weighty = 1.0;
		panel.add(new JPanel(), cons);
		return property;
	}

	// Open up the file save dialog
	public void save(boolean saveAs) {
		if(panel.file == null || saveAs) {
			FileDialog fileDialog = new FileDialog(frame, "Save As", FileDialog.SAVE);
			fileDialog.setVisible(true);
			if(fileDialog.getFile() == null) {
				return;
			}
			panel.file = new File(fileDialog.getDirectory(), fileDialog.getFile());
		}
		try {
			panel.save();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	// Open up the file open dialog
	public void open() {
		FileDialog fileDialog = new FileDialog(frame, "Open", FileDialog.LOAD);
		fileDialog.setVisible(true);
		if(fileDialog.getFile() == null) {
			return;
		}
		panel.file = new File(fileDialog.getDirectory(), fileDialog.getFile());
		try {
			panel.load();
			properties.removeAll();
			generatePanel(properties, DummyProperties.class, panel.properties);
			properties.revalidate();
			properties.repaint();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// Set some properties, for aesthetic and performance reasons
		System.setProperty("sun.java2d.opengl","true");
		UIManager.put("Slider.paintValue", false);
		new MapMaker();
	}
}
