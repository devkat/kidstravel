package spatutorial.client.services

import diode.ActionResult._
import diode.RootModelRW
import diode.data._
import kidstravel.client.services._
import kidstravel.shared.poi.Poi
import utest._

object SPACircuitTests extends TestSuite {
  def tests = TestSuite {
    /*
      'TodoHandler - {
        val model = Ready(Pois(Seq(
          Poi(0, "Test1"),
          Poi(1, "Test2"),
          Poi(2, "Test3")
        )))

        val newTodosPois = Seq(
          Poi(3, "Test3")
        )

        def build = new PoiHandler(new RootModelRW(model))

        'RefreshPois - {
          val h = build
          val result = h.handle(RefreshPois)
          result match {
            case EffectOnly(effects) =>
              assert(effects.size == 1)
            case _ =>
              assert(false)
          }
        }

        'UpdateAllPois - {
          val h = build
          val result = h.handle(UpdateAllPois(newTodosPois))
          assert(result == ModelUpdate(Ready(Pois(newTodosPois))))
        }

        'UpdatePoiAdd - {
          val h = build
          val result = h.handle(UpdatePoi(Poi(4, "Test4")))
          result match {
            case ModelUpdateEffect(newValue, effects) =>
              assert(newValue.get.pois.size == 4)
              assert(newValue.get.pois(3).id == "4")
              assert(effects.size == 1)
            case _ =>
              assert(false)
          }
        }

        'UpdatePoi - {
          val h = build
          val result = h.handle(UpdatePoi(Poi(1, "Test111")))
          result match {
            case ModelUpdateEffect(newValue, effects) =>
              assert(newValue.get.pois.size == 3)
              assert(newValue.get.pois.head.name == "Test111")
              assert(effects.size == 1)
            case _ =>
              assert(false)
          }
        }

        'DeletePoi - {
          val h = build
          val result = h.handle(DeletePoi(model.get.pois.head))
          result match {
            case ModelUpdateEffect(newValue, effects) =>
              assert(newValue.get.pois.size == 2)
              assert(newValue.get.pois.head.name == "Test2")
              assert(effects.size == 1)
            case _ =>
              assert(false)
          }
        }
      }
    */

    }
}
