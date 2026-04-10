package net.shoreline.client;

/**
 * @author linus
 * @since 1.0
 */
public class ShutdownHook extends Thread
{
    public ShutdownHook()
    {
        setName(ShorelineMod.MOD_NAME + "-ShutdownHook");
    }

    /**
     * This runs when the game is shutdown
     */
    @Override
    public void run()
    {
        Shoreline.CONFIG.saveModConfiguration();
    }
}
