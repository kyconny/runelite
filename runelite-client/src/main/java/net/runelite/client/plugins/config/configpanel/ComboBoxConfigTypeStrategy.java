package net.runelite.client.plugins.config.configpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import javax.inject.Singleton;
import javax.swing.JComboBox;
import javax.swing.ListCellRenderer;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigItemDescriptor;
import net.runelite.client.ui.components.ComboBoxListRenderer;
import net.runelite.client.util.Text;

@Singleton
public class ComboBoxConfigTypeStrategy implements ConfigTypeStrategy<Enum<?>>
{
	private final ListCellRenderer<Enum<?>> listCellRenderer = new ComboBoxListRenderer<>();

	@Override
	public boolean appliesToType(Type type)
	{
		return type instanceof Class<?> && ((Class<?>) type).isEnum();
	}

	@Override
	public String getBorderLayout()
	{
		return BorderLayout.EAST;
	}

	@Override
	public Component createComponent(BiConsumer<Component, Enum<?>> onNewValue, Component parent, ConfigDescriptor cd, ConfigItemDescriptor cid, Enum<?> existingValue)
	{
		JComboBox<Enum<?>> box = new JComboBox<Enum<?>>(existingValue.getClass().getEnumConstants()); // NOPMD: UseDiamondOperator

		// set renderer prior to calling box.getPreferredSize(), since it will invoke the renderer
		// to build components for each combobox element in order to compute the display size of the
		// combobox
		box.setRenderer(listCellRenderer);
		box.setPreferredSize(new Dimension(box.getPreferredSize().width, 25));
		box.setForeground(Color.WHITE);
		box.setFocusable(false);

		box.setSelectedItem(existingValue);
		box.setToolTipText(Text.titleCase(existingValue));

		box.addItemListener(e ->
		{
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				Enum<?> newValue = (Enum<?>) box.getSelectedItem();
				onNewValue.accept(box, newValue);
				box.setToolTipText(Text.titleCase(newValue));
			}
		});

		return box;
	}
}
