package net.runelite.client.plugins.config.configpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigItemDescriptor;

@Singleton
public class TextFieldConfigTypeStrategy implements ConfigTypeStrategy<String>
{
	@Override
	public boolean appliesToType(Type type)
	{
		return type == String.class;
	}

	@Override
	public String getBorderLayout()
	{
		return BorderLayout.SOUTH;
	}

	@Override
	public Component createComponent(BiConsumer<Component, String> onNewValue, Component parent, ConfigDescriptor cd, ConfigItemDescriptor cid, String existingValue)
	{
		JTextComponent textField;

		if (cid.getItem().secret())
		{
			textField = new JPasswordField();
		}
		else
		{
			final JTextArea textArea = new JTextArea();
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textField = textArea;
		}

		textField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		textField.setText(existingValue);

		textField.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				onNewValue.accept(textField, textField.getText());
			}
		});

		return textField;
	}
}
