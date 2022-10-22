package net.runelite.client.plugins.config.configpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import javax.inject.Singleton;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigItemDescriptor;

@Singleton
public class FileChooserConfigTypeStrategy implements ConfigTypeStrategy<File>
{
	@Override
	public boolean appliesToType(Type type)
	{
		return type == File.class;
	}

	@Override
	public String getBorderLayout()
	{
		return BorderLayout.EAST;
	}

	@Override
	public Component createComponent(BiConsumer<Component, File> onNewValue, Component parent, ConfigDescriptor cd, ConfigItemDescriptor cid, File existingValue)
	{
		JButton button = new JButton();

		if (existingValue != null)
		{
			button.setText(existingValue.getName());
		}
		else
		{
			button.setText("Choose File");
		}

		JFileChooser chooser = new JFileChooser();
		String[] supportedExtensions = cid.getItem().supportedFileExtensions();

		if (supportedExtensions.length > 0)
		{
			FileNameExtensionFilter fileNameExtensionFilter = new FileNameExtensionFilter(null, cid.getItem().supportedFileExtensions());
			chooser.setFileFilter(fileNameExtensionFilter);
		}

		button.addActionListener(ac -> {
			int retVal = chooser.showOpenDialog(parent);

			if (retVal == JFileChooser.APPROVE_OPTION)
			{
				File selectedFile = chooser.getSelectedFile();

				button.setText(selectedFile.getName());
				onNewValue.accept(button, selectedFile);
			}
		});

		return button;
	}
}
