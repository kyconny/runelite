package net.runelite.client.plugins.config.configpanel;

import com.google.common.base.MoreObjects;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.inject.Singleton;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigItemDescriptor;
import net.runelite.client.ui.components.ComboBoxListRenderer;
import org.apache.commons.lang3.ArrayUtils;

@Singleton
public class ListConfigTypeStrategy implements ConfigTypeStrategy<Set<? extends Enum>>
{
	private final ListCellRenderer<Enum<?>> listCellRenderer = new ComboBoxListRenderer<>();
	@Override
	public boolean appliesToType(Type type)
	{
		return type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() == Set.class;
	}

	@Override
	public String getBorderLayout()
	{
		return BorderLayout.EAST;
	}

	@Override
	public Component createComponent(BiConsumer<Component, Set<? extends Enum>> onNewValue, Component parent, ConfigDescriptor cd, ConfigItemDescriptor cid, Set<? extends Enum> existingValue)
	{
		ParameterizedType parameterizedType = (ParameterizedType) cid.getType();
		Class<? extends Enum> type = (Class<? extends Enum>) parameterizedType.getActualTypeArguments()[0];

		JList<Enum<?>> list = new JList<Enum<?>>(type.getEnumConstants()); // NOPMD: UseDiamondOperator
		list.setCellRenderer(listCellRenderer);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setSelectedIndices(
			MoreObjects.firstNonNull(existingValue, Collections.emptySet())
				.stream()
				.mapToInt(e -> ArrayUtils.indexOf(type.getEnumConstants(), e))
				.toArray());
		list.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				onNewValue.accept(list, new HashSet<>(list.getSelectedValuesList()));
			}
		});

		return list;
	}
}
