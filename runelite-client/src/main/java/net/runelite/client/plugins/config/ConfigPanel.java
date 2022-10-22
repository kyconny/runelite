/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.config;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableSet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigItemDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.ConfigObject;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.ConfigSectionDescriptor;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ExternalPluginsChanged;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.externalplugins.ExternalPluginManager;
import net.runelite.client.externalplugins.ExternalPluginManifest;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.config.configpanel.CheckBoxConfigTypeStrategy;
import net.runelite.client.plugins.config.configpanel.ColorPickerConfigTypeStrategy;
import net.runelite.client.plugins.config.configpanel.ComboBoxConfigTypeStrategy;
import net.runelite.client.plugins.config.configpanel.ConfigTypeStrategy;
import net.runelite.client.plugins.config.configpanel.DimensionConfigTypeStrategy;
import net.runelite.client.plugins.config.configpanel.DoubleSpinnerConfigTypeStrategy;
import net.runelite.client.plugins.config.configpanel.IntSpinnerConfigTypeStrategy;
import net.runelite.client.plugins.config.configpanel.KeyBindConfigTypeStrategy;
import net.runelite.client.plugins.config.configpanel.ListConfigTypeStrategy;
import net.runelite.client.plugins.config.configpanel.TextFieldConfigTypeStrategy;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

@Slf4j
class ConfigPanel extends PluginPanel
{
	private static final ImageIcon SECTION_EXPAND_ICON;
	private static final ImageIcon SECTION_EXPAND_ICON_HOVER;
	private static final ImageIcon SECTION_RETRACT_ICON;
	private static final ImageIcon SECTION_RETRACT_ICON_HOVER;
	static final ImageIcon BACK_ICON;
	static final ImageIcon BACK_ICON_HOVER;

	private static final Map<ConfigSectionDescriptor, Boolean> sectionExpandStates = new HashMap<>();

	static
	{
		final BufferedImage backIcon = ImageUtil.loadImageResource(ConfigPanel.class, "config_back_icon.png");
		BACK_ICON = new ImageIcon(backIcon);
		BACK_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(backIcon, -100));

		BufferedImage sectionRetractIcon = ImageUtil.loadImageResource(ConfigPanel.class, "/util/arrow_right.png");
		sectionRetractIcon = ImageUtil.luminanceOffset(sectionRetractIcon, -121);
		SECTION_EXPAND_ICON = new ImageIcon(sectionRetractIcon);
		SECTION_EXPAND_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(sectionRetractIcon, -100));
		final BufferedImage sectionExpandIcon = ImageUtil.rotateImage(sectionRetractIcon, Math.PI / 2);
		SECTION_RETRACT_ICON = new ImageIcon(sectionExpandIcon);
		SECTION_RETRACT_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(sectionExpandIcon, -100));
	}

	private final ConfigManager configManager;
	private final PluginManager pluginManager;
	private final ExternalPluginManager externalPluginManager;

	private final Set<ConfigTypeStrategy<?>> configTypeStrategies;

	private final FixedWidthPanel mainPanel;
	private final JLabel title;
	private final PluginToggleButton pluginToggle;

	private final PluginListPanel pluginListPanel;

	private PluginConfigurationDescriptor pluginConfig = null;

	@Inject
	private ConfigPanel(PluginListPanel pluginListPanel, ConfigManager configManager, PluginManager pluginManager,
						ExternalPluginManager externalPluginManager, CheckBoxConfigTypeStrategy checkBoxConfigTypeStrategy,
						ColorPickerConfigTypeStrategy colorPickerConfigTypeStrategy, ComboBoxConfigTypeStrategy comboBoxConfigTypeStrategy,
						DimensionConfigTypeStrategy dimensionConfigTypeStrategy, DoubleSpinnerConfigTypeStrategy doubleSpinnerConfigTypeStrategy,
						IntSpinnerConfigTypeStrategy intSpinnerConfigTypeStrategy, KeyBindConfigTypeStrategy keyBindConfigTypeStrategy,
						ListConfigTypeStrategy listConfigTypeStrategy, TextFieldConfigTypeStrategy textFieldConfigTypeStrategy)
	{
		super(false);

		this.pluginListPanel = pluginListPanel;
		this.configManager = configManager;
		this.pluginManager = pluginManager;
		this.externalPluginManager = externalPluginManager;

		ImmutableSet.Builder<ConfigTypeStrategy<?>> strategyBuilder = ImmutableSet.builder();
		strategyBuilder.add(checkBoxConfigTypeStrategy);
		strategyBuilder.add(colorPickerConfigTypeStrategy);
		strategyBuilder.add(comboBoxConfigTypeStrategy);
		strategyBuilder.add(dimensionConfigTypeStrategy);
		strategyBuilder.add(doubleSpinnerConfigTypeStrategy);
		strategyBuilder.add(intSpinnerConfigTypeStrategy);
		strategyBuilder.add(keyBindConfigTypeStrategy);
		strategyBuilder.add(listConfigTypeStrategy);
		strategyBuilder.add(textFieldConfigTypeStrategy);
		configTypeStrategies = strategyBuilder.build();

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel topPanel = new JPanel();
		topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		topPanel.setLayout(new BorderLayout(0, BORDER_OFFSET));
		add(topPanel, BorderLayout.NORTH);

		mainPanel = new FixedWidthPanel();
		mainPanel.setBorder(new EmptyBorder(8, 10, 10, 10));
		mainPanel.setLayout(new DynamicGridLayout(0, 1, 0, 5));
		mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel northPanel = new FixedWidthPanel();
		northPanel.setLayout(new BorderLayout());
		northPanel.add(mainPanel, BorderLayout.NORTH);

		JScrollPane scrollPane = new JScrollPane(northPanel);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);

		JButton topPanelBackButton = new JButton(BACK_ICON);
		topPanelBackButton.setRolloverIcon(BACK_ICON_HOVER);
		SwingUtil.removeButtonDecorations(topPanelBackButton);
		topPanelBackButton.setPreferredSize(new Dimension(22, 0));
		topPanelBackButton.setBorder(new EmptyBorder(0, 0, 0, 5));
		topPanelBackButton.addActionListener(e -> pluginListPanel.getMuxer().popState());
		topPanelBackButton.setToolTipText("Back");
		topPanel.add(topPanelBackButton, BorderLayout.WEST);

		pluginToggle = new PluginToggleButton();
		topPanel.add(pluginToggle, BorderLayout.EAST);
		title = new JLabel();
		title.setForeground(Color.WHITE);

		topPanel.add(title);
	}

	void init(PluginConfigurationDescriptor pluginConfig)
	{
		assert this.pluginConfig == null;
		this.pluginConfig = pluginConfig;

		String name = pluginConfig.getName();
		title.setText(name);
		title.setForeground(Color.WHITE);
		title.setToolTipText("<html>" + name + ":<br>" + pluginConfig.getDescription() + "</html>");

		ExternalPluginManifest mf = pluginConfig.getExternalPluginManifest();
		JMenuItem uninstallItem = null;
		if (mf != null)
		{
			uninstallItem = new JMenuItem("Uninstall");
			uninstallItem.addActionListener(ev -> externalPluginManager.remove(mf.getInternalName()));
		}

		PluginListItem.addLabelPopupMenu(title, pluginConfig.createSupportMenuItem(), uninstallItem);

		if (pluginConfig.getPlugin() != null)
		{
			pluginToggle.setConflicts(pluginConfig.getConflicts());
			pluginToggle.setSelected(pluginManager.isPluginEnabled(pluginConfig.getPlugin()));
			pluginToggle.addItemListener(i ->
			{
				if (pluginToggle.isSelected())
				{
					pluginListPanel.startPlugin(pluginConfig.getPlugin());
				}
				else
				{
					pluginListPanel.stopPlugin(pluginConfig.getPlugin());
				}
			});
		}
		else
		{
			pluginToggle.setVisible(false);
		}

		rebuild();
	}

	private void toggleSection(ConfigSectionDescriptor csd, JButton button, JPanel contents)
	{
		boolean newState = !contents.isVisible();
		contents.setVisible(newState);
		button.setIcon(newState ? SECTION_RETRACT_ICON : SECTION_EXPAND_ICON);
		button.setRolloverIcon(newState ? SECTION_RETRACT_ICON_HOVER : SECTION_EXPAND_ICON_HOVER);
		button.setToolTipText(newState ? "Retract" : "Expand");
		sectionExpandStates.put(csd, newState);
		SwingUtilities.invokeLater(contents::revalidate);
	}

	private void rebuild()
	{
		mainPanel.removeAll();

		ConfigDescriptor cd = pluginConfig.getConfigDescriptor();

		final Map<String, JPanel> sectionWidgets = new HashMap<>();
		final Map<ConfigObject, JPanel> topLevelPanels = new TreeMap<>((a, b) ->
			ComparisonChain.start()
			.compare(a.position(), b.position())
			.compare(a.name(), b.name())
			.result());

		for (ConfigSectionDescriptor csd : cd.getSections())
		{
			ConfigSection cs = csd.getSection();
			final boolean isOpen = sectionExpandStates.getOrDefault(csd, !cs.closedByDefault());

			final JPanel section = new JPanel();
			section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
			section.setMinimumSize(new Dimension(PANEL_WIDTH, 0));

			final JPanel sectionHeader = new JPanel();
			sectionHeader.setLayout(new BorderLayout());
			sectionHeader.setMinimumSize(new Dimension(PANEL_WIDTH, 0));
			// For whatever reason, the header extends out by a single pixel when closed. Adding a single pixel of
			// border on the right only affects the width when closed, fixing the issue.
			sectionHeader.setBorder(new CompoundBorder(
				new MatteBorder(0, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR),
				new EmptyBorder(0, 0, 3, 1)));
			section.add(sectionHeader, BorderLayout.NORTH);

			final JButton sectionToggle = new JButton(isOpen ? SECTION_RETRACT_ICON : SECTION_EXPAND_ICON);
			sectionToggle.setRolloverIcon(isOpen ? SECTION_RETRACT_ICON_HOVER : SECTION_EXPAND_ICON_HOVER);
			sectionToggle.setPreferredSize(new Dimension(18, 0));
			sectionToggle.setBorder(new EmptyBorder(0, 0, 0, 5));
			sectionToggle.setToolTipText(isOpen ? "Retract" : "Expand");
			SwingUtil.removeButtonDecorations(sectionToggle);
			sectionHeader.add(sectionToggle, BorderLayout.WEST);

			String name = cs.name();
			final JLabel sectionName = new JLabel(name);
			sectionName.setForeground(ColorScheme.BRAND_ORANGE);
			sectionName.setFont(FontManager.getRunescapeBoldFont());
			sectionName.setToolTipText("<html>" + name + ":<br>" + cs.description() + "</html>");
			sectionHeader.add(sectionName, BorderLayout.CENTER);

			final JPanel sectionContents = new JPanel();
			sectionContents.setLayout(new DynamicGridLayout(0, 1, 0, 5));
			sectionContents.setMinimumSize(new Dimension(PANEL_WIDTH, 0));
			sectionContents.setBorder(new CompoundBorder(
				new MatteBorder(0, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR),
				new EmptyBorder(BORDER_OFFSET, 0, BORDER_OFFSET, 0)));
			sectionContents.setVisible(isOpen);
			section.add(sectionContents, BorderLayout.SOUTH);

			// Add listeners to each part of the header so that it's easier to toggle them
			final MouseAdapter adapter = new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent e)
				{
					toggleSection(csd, sectionToggle, sectionContents);
				}
			};
			sectionToggle.addActionListener(actionEvent -> toggleSection(csd, sectionToggle, sectionContents));
			sectionName.addMouseListener(adapter);
			sectionHeader.addMouseListener(adapter);

			sectionWidgets.put(csd.getKey(), sectionContents);

			topLevelPanels.put(csd, section);
		}

		for (ConfigItemDescriptor cid : cd.getItems())
		{
			if (cid.getItem().hidden())
			{
				continue;
			}

			Type type = cid.getType();


			JPanel item = new JPanel();
			item.setLayout(new BorderLayout());
			item.setMinimumSize(new Dimension(PANEL_WIDTH, 0));
			String name = cid.getItem().name();
			JLabel configEntryName = new JLabel(name);
			configEntryName.setForeground(Color.WHITE);
			configEntryName.setToolTipText("<html>" + name + ":<br>" + cid.getItem().description() + "</html>");
			PluginListItem.addLabelPopupMenu(configEntryName, createResetMenuItem(pluginConfig, cid));
			item.add(configEntryName, BorderLayout.CENTER);

			createComponent(type, cd, cid, item);

			JPanel section = sectionWidgets.get(cid.getItem().section());
			if (section == null)
			{
				topLevelPanels.put(cid, item);
			}
			else
			{
				section.add(item);
			}
		}

		topLevelPanels.values().forEach(mainPanel::add);

		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener((e) ->
		{
			final int result = JOptionPane.showOptionDialog(resetButton, "Are you sure you want to reset this plugin's configuration?",
				"Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
				null, new String[]{"Yes", "No"}, "No");

			if (result == JOptionPane.YES_OPTION)
			{
				configManager.setDefaultConfiguration(pluginConfig.getConfig(), true);

				// Reset non-config panel keys
				Plugin plugin = pluginConfig.getPlugin();
				if (plugin != null)
				{
					plugin.resetConfiguration();
				}

				rebuild();
			}
		});
		mainPanel.add(resetButton);

		JButton backButton = new JButton("Back");
		backButton.addActionListener(e -> pluginListPanel.getMuxer().popState());
		mainPanel.add(backButton);

		revalidate();
	}

	@SuppressWarnings("unchecked")
	private <T> void createComponent(Type type, ConfigDescriptor cd, ConfigItemDescriptor cid, JPanel item) {
		Optional<ConfigTypeStrategy<?>> maybeConfigTypeStrategy = configTypeStrategies.stream()
			.filter(s -> s.appliesToType(type))
			.findFirst();

		if (!maybeConfigTypeStrategy.isPresent()) {
			log.error("Failed to find config strategy for type " + type + " skipping.");
			return;
		}

		String groupName = cd.getGroup().value();
		String itemKey = cid.getItem().keyName();

		ConfigTypeStrategy<T> strategy = (ConfigTypeStrategy<T>) maybeConfigTypeStrategy.get();
		T existingValue = configManager.getConfiguration(groupName, itemKey, type);

		BiConsumer<Component, T> onNewValue = (c, v) -> changeConfiguration(c, cd, cid, v);

		Component component = strategy.createComponent(onNewValue, this, cd, cid, existingValue);
		item.add(component, strategy.getBorderLayout());
	}

	private void changeConfiguration(Component component, ConfigDescriptor cd, ConfigItemDescriptor cid, Object value)
	{

		final ConfigItem configItem = cid.getItem();

		if (!Strings.isNullOrEmpty(configItem.warning()))
		{
			final int result = JOptionPane.showOptionDialog(component, configItem.warning(),
				"Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
				null, new String[]{"Yes", "No"}, "No");

			if (result != JOptionPane.YES_OPTION)
			{
				rebuild();
				return;
			}
		}

		configManager.setConfiguration(cd.getGroup().value(), cid.getItem().keyName(), value);
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(PANEL_WIDTH + SCROLLBAR_WIDTH, super.getPreferredSize().height);
	}

	@Subscribe
	public void onPluginChanged(PluginChanged event)
	{
		if (event.getPlugin() == this.pluginConfig.getPlugin())
		{
			SwingUtilities.invokeLater(() ->
			{
				pluginToggle.setSelected(event.isLoaded());
			});
		}
	}

	@Subscribe
	private void onExternalPluginsChanged(ExternalPluginsChanged ev)
	{
		if (pluginManager.getPlugins().stream()
			.noneMatch(p -> p == this.pluginConfig.getPlugin()))
		{
			pluginListPanel.getMuxer().popState();
		}
		SwingUtilities.invokeLater(this::rebuild);
	}

	private JMenuItem createResetMenuItem(PluginConfigurationDescriptor pluginConfig, ConfigItemDescriptor configItemDescriptor)
	{
		JMenuItem menuItem = new JMenuItem("Reset");
		menuItem.addActionListener(e ->
		{
			ConfigDescriptor configDescriptor = pluginConfig.getConfigDescriptor();
			ConfigGroup configGroup = configDescriptor.getGroup();
			ConfigItem configItem = configItemDescriptor.getItem();

			// To reset one item we'll just unset it and then apply defaults over the whole group
			configManager.unsetConfiguration(configGroup.value(), configItem.keyName());
			configManager.setDefaultConfiguration(pluginConfig.getConfig(), false);

			rebuild();
		});
		return menuItem;
	}
}
