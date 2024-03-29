package example

import net.rwhps.server.data.EventManage
import net.rwhps.server.data.HessModuleManage
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.player.PlayerHess
import net.rwhps.server.data.plugin.PluginData
import net.rwhps.server.func.StrCons
import net.rwhps.server.net.Administration
import net.rwhps.server.plugin.Plugin
import net.rwhps.server.util.Time.concurrentMillis
import net.rwhps.server.util.Time.getUtcMilliFormat
import net.rwhps.server.util.game.CommandHandler
import net.rwhps.server.util.log.Log.info
import java.util.*

class MainKt : Plugin() {
    private var pluginData = PluginData()

    /**
     * 这里主要做初始化
     */
    override fun init() {
        // 设置Bin文件位置
        pluginData.setFileUtil(pluginDataFileUtils.toFile("ExampleData.bin"))
        pluginData.read()

        //过滤消息
        Data.core.admin.addChatFilter(object : Administration.ChatFilter {
            override fun filter(player: PlayerHess, message: String?): String? {
                if (message == null) {
                    return null
                }
                return message.replace("heck", "h*ck")
            }
        })

        //读取数据
        val lastStartTime = pluginData.getData("lastStartTime", concurrentMillis())
        val lastStartTimeString = pluginData.getData("lastStartTimeString", getUtcMilliFormat(1))
        info("lastStartTime", lastStartTime)
        info("lastStartTimeString", lastStartTimeString)
        pluginData.setData("lastStartTime", concurrentMillis())
        pluginData.setData("lastStartTimeString", getUtcMilliFormat(1))
    }

    override fun registerEvents(eventManage: EventManage) {
        eventManage.registerListener(Event())
    }
    /**
     * 注册服务器命令
     */
    override fun registerServerCommands(handler: CommandHandler) {
        handler.register("hi", "#这是Server命令简介") { _: Array<String?>?, log: StrCons ->
            log("hi")
        }

        handler.register("arg", "<这是必填> [这是选填]", "#这是Server命令简介") { arg: Array<String?>?, log: StrCons ->
            log(arg.contentToString())
        }

        handler.register("args", "<这是必填...>", "#这是Server命令简介") { arg: Array<String?>, log: StrCons ->
            log(arg[0])
        }
    }

    /**
     * 注册客户端命令
     * @param handler
     */
    override fun registerServerClientCommands(handler: CommandHandler) {
        //向自己回复消息
        handler.register("reply", "<text...>", "#只取第一个回复.") { args: Array<String>, player: PlayerHess ->
            player.sendSystemMessage("你发的是: " + args[0])
        }

        //向玩家发送
        handler.register("whisper", "<player> <text...>", "#向另一个玩家发消息.") { args: Array<String>, player: PlayerHess ->
            //查找玩家
            val other: PlayerHess? = HessModuleManage.hps.room.playerManage.playerGroup.find { p: PlayerHess -> p.name.equals(args[0], ignoreCase = true) }
            if (other == null) {
                player.sendSystemMessage("找不到这个玩家!")
                return@register
            }

            //向玩家发消息
            other.sendSystemMessage("玩家: " + player.name + " 向你发送: " + args[1])
        }
    }

    override fun onDisable() {
        super.onDisable()
        //Custom
        pluginData.save()
    }
}