package net.runelite.client.plugins.config.configpanel;

import java.awt.Component;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigItemDescriptor;

public interface ConfigTypeStrategy<T>
{
	boolean appliesToType(Type type);
	String getBorderLayout();

	Component createComponent(BiConsumer<Component, T> onNewValue, Component parent, ConfigDescriptor cd, ConfigItemDescriptor cid, T existingValue);
}
