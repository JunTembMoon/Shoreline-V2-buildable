package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.preset.ModulePreset;
import net.shoreline.client.impl.Managers;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ConfigCommand extends Command
{
    private static final String FILE_PREFIX = "config-";
    private static final String MODULES_SCOPE = "modules";
    private static final String RENDER_SCOPE = "render";
    private static final String COMBAT_SCOPE = "combat";

    public ConfigCommand()
    {
        super("config", "save/load modules, render, and combat configs");
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> argumentBuilder)
    {
        argumentBuilder.then(buildArgument("save/load", StringArgumentType.string())
                .suggests(buildSuggestions("save", "load"))
                .then(buildArgument("name", StringArgumentType.string())
                        .executes(context ->
                                executeAll(StringArgumentType.getString(context, "save/load"),
                                        StringArgumentType.getString(context, "name")))))
                .then(buildArgument("modules/render/combat", StringArgumentType.string())
                        .suggests(buildSuggestions(MODULES_SCOPE, RENDER_SCOPE, COMBAT_SCOPE))
                        .then(buildArgument("save/load", StringArgumentType.string())
                                .suggests(buildSuggestions("save", "load"))
                                .then(buildArgument("name", StringArgumentType.string())
                                        .executes(context ->
                                                executeScope(StringArgumentType.getString(context, "modules/render/combat"),
                                                        StringArgumentType.getString(context, "save/load"),
                                                        StringArgumentType.getString(context, "name"))))));
    }

    private int executeAll(String action, String name)
    {
        try
        {
            List<ModulePreset<Module>> presets = List.of(
                    createPreset(MODULES_SCOPE, name),
                    createPreset(RENDER_SCOPE, name),
                    createPreset(COMBAT_SCOPE, name)
            );

            if (action.equalsIgnoreCase("save"))
            {
                for (ModulePreset<Module> preset : presets)
                {
                    preset.saveFile();
                }
                sendClientChatMessage("Successfully saved modules, render, and combat configs!");
                return 1;
            }

            if (action.equalsIgnoreCase("load"))
            {
                for (ModulePreset<Module> preset : presets)
                {
                    preset.loadFile();
                }
                sendClientChatMessage("Successfully loaded modules, render, and combat configs!");
                return 1;
            }
        }
        catch (IOException e)
        {
            sendErrorChatMessage("Failed to load/save config set.");
            return 0;
        }

        sendErrorChatMessage("Unknown action.");
        return 0;
    }

    private int executeScope(String scope, String action, String name)
    {
        try
        {
            ModulePreset<Module> preset = createPreset(scope, name);
            if (preset == null)
            {
                sendErrorChatMessage("Unknown config scope.");
                return 0;
            }

            if (action.equalsIgnoreCase("save"))
            {
                preset.saveFile();
                sendClientChatMessage("Successfully saved " + normalizeScope(scope) + " config!");
                return 1;
            }

            if (action.equalsIgnoreCase("load"))
            {
                preset.loadFile();
                sendClientChatMessage("Successfully loaded " + normalizeScope(scope) + " config!");
                return 1;
            }
        }
        catch (IOException e)
        {
            sendErrorChatMessage("Failed to load/save config.");
            return 0;
        }

        sendErrorChatMessage("Unknown action.");
        return 0;
    }

    private ModulePreset<Module> createPreset(String scope, String name) throws IOException
    {
        return switch (normalizeScope(scope))
        {
            case MODULES_SCOPE -> new ModulePreset<>(buildFilename(MODULES_SCOPE, name), Managers.MODULES.getModules());
            case RENDER_SCOPE -> new ModulePreset<>(buildFilename(RENDER_SCOPE, name),
                    Managers.MODULES.getModules(module -> module.getCategory() == GuiCategory.RENDER));
            case COMBAT_SCOPE -> new ModulePreset<>(buildFilename(COMBAT_SCOPE, name),
                    Managers.MODULES.getModules(module -> module.getCategory() == GuiCategory.COMBAT));
            default -> null;
        };
    }

    private String buildFilename(String scope, String name)
    {
        return FILE_PREFIX + scope + "-" + name;
    }

    private String normalizeScope(String scope)
    {
        String normalized = scope.toLowerCase(Locale.ROOT);
        if (normalized.equals("renders"))
        {
            return RENDER_SCOPE;
        }

        return normalized;
    }
}