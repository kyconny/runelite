package net.runelite.client.plugins.config.configpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigItemDescriptor;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.ModifierlessKeybind;

@Singleton
public class KeyBindConfigTypeStrategy implements ConfigTypeStrategy<Keybind>
{
	@Override
	public boolean appliesToType(Type type)
	{
		return type == Keybind.class || type == ModifierlessKeybind.class;
	}

	@Override
	public String getBorderLayout()
	{
		return BorderLayout.EAST;
	}

	@Override
	public Component createComponent(BiConsumer<Component, Keybind> onNewValue, Component parent, ConfigDescriptor cd, ConfigItemDescriptor cid, Keybind existingValue)
	{
		HotkeyButton button = new HotkeyButton(existingValue, existingValue instanceof ModifierlessKeybind);

		button.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				onNewValue.accept(button, button.getValue());
			}
		});

		return button;
	}
}
