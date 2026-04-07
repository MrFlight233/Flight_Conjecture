package org.flight.flight_conjecture.client;


import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.flight.flight_conjecture.Flight_conjecture;
import org.flight.flight_conjecture.container.screen.IndustrialProcessingUnitScreen;
import org.flight.flight_conjecture.init.ModMenuTypes;

/**
 * 客户端初始化类。
 * <p>
 * 该类只在客户端环境加载，用于注册所有与客户端相关的内容，
 * 例如 Screen、渲染器、模型层等。
 * <p>
 * 这里我们主要完成一件事：注册 Menu 与 Screen 的对应关系。
 */
@Mod.EventBusSubscriber(modid = Flight_conjecture.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        registerScreens();
    }

    private static void registerScreens() {
        MenuScreens.register(ModMenuTypes.INDUSTRIAL_PROCESSING_UNIT_MENU.get(), IndustrialProcessingUnitScreen::new);
    }

}
