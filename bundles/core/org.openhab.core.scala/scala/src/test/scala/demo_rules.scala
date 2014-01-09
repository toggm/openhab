import org.openhab.core.scala.RuleSetFactory
import hammurabi.Rule
import hammurabi.Rule._
import org.openhab.core.items.Item
import org.openhab.core.scala.model.BusEvent._
import org.openhab.core.types.Command
import org.openhab.core.library.types.OnOffType
import org.openhab.core.library.items.NumberItem
import org.openhab.core.library.types.DecimalType
import org.openhab.core.library.items.SwitchItem
import scala.actors.Logger
import hammurabi.util.Logger
import org.openhab.core.scala.model._
import org.openhab.core.scala.model.SystemEventType._
import org.openhab.io.multimedia.actions.Audio._
import org.openhab.core.library.types.IncreaseDecreaseType._

object ExampleRuleSetFactoryImpl extends RuleSetFactory {
  
  def dimm(item: Item, cmd: Command) = {
    var percent = 0
    if(item.getState().isInstanceOf[DecimalType]) {
      percent = item.getState().asInstanceOf[DecimalType].intValue()
    }
        
    percent = percent + (cmd match {
      case INCREASE => +5
      case DECREASE => -5
    })
		
	if(percent<0)   percent = 0
	if(percent>100) percent = 100
	
	updated(item).to(new DecimalType(percent))
  }
  
  override def generateRuleSet(): Set[Rule] = {
    Set(
      rule("System State") let {
        val e = any(kindOf[SystemEvent])
        
        then {
        	e.eventType match {          
        	case Startup => say("Welcome at openHab!")
        	case Shutdown => say("Good bye!")
        	}
        }
      },
      rule("Dimmed Light1") let {
        val e = kindOf[CommandEvent] having (_.item.getName() == "DimmedLight1")
        then {
        	dimm(e.item, e.cmd)
        }
      },
      rule("Dimmed Light2") let {
        val e = kindOf[CommandEvent] having (_.item.getName() == "DimmedLight2")
        then {
        	dimm(e.item, e.cmd)
        }
      },
      rule("Select Radio Station") let {
        val e = kindOf[CommandEvent] having (_.item.getName() == "Radio_Station")
        val cmd = e.cmd
        when (cmd.isInstanceOf[DecimalType])
        then {
	        val cmdVal = cmd.asInstanceOf[DecimalType].intValue()
	        cmdVal match {
	          case 0 => playStream(null)
				case 1 => playStream("http://metafiles.gl-systemhaus.de/hr/hr3_2.m3u")
				case 2 => playStream("http://mp3-live.swr3.de/swr3_m.m3u")
				case 3 => playStream("http://edge.live.mp3.mdn.newmedia.nacamar.net/radioffh/livestream.mp3.m3u")
	        }
        }
      })
  }
}