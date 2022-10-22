package net.runelite.client.plugins.config.configpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import javax.inject.Singleton;
import javax.swing.JCheckBox;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigItemDescriptor;
import net.runelite.client.ui.ColorScheme;

@Singleton
public class CheckBoxConfigTypeStrategy implements ConfigTypeStrategy<Boolean>
{

	@Override
	public boolean appliesToType(Type type)
	{
		return type == boolean.class;
	}

	@Override
	public String getBorderLayout()
	{
		return BorderLayout.EAST;
	}

	@Override
	public Component createComponent(BiConsumer<Component, Boolean> onNewValue, Component parent, ConfigDescriptor cd, ConfigItemDescriptor cid, Boolean existingValue)
	{
		JCheckBox checkbox = new JCheckBox();
		checkbox.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
		checkbox.setSelected(existingValue);
		checkbox.addActionListener(al -> onNewValue.accept(checkbox, checkbox.isSelected()));
		return checkbox;
	}

}
