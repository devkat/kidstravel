package kidstravel.client.modules

import diode.data.Pot
import diode.react.ReactPot._
import diode.react._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import kidstravel.client.components.Bootstrap._
import kidstravel.client.components._
import kidstravel.client.logger._
import kidstravel.client.services._
import kidstravel.shared.poi.Poi

import scalacss.ScalaCssReact._

object PoiModule {

  case class Props(proxy: ModelProxy[Pot[Pois]])

  case class State(selectedItem: Option[Poi] = None, showTodoForm: Boolean = false)

  class Backend($: BackendScope[Props, State]) {
    def mounted(props: Props) =
      // dispatch a message to refresh the todos, which will cause TodoStore to fetch todos from the server
      Callback.when(props.proxy().isEmpty)(props.proxy.dispatch(RefreshPois))

    def editTodo(item: Option[Poi]) =
      // activate the edit dialog
      $.modState(s => s.copy(selectedItem = item, showTodoForm = true))

    def todoEdited(poi: Poi, cancelled: Boolean) = {
      val cb = if (cancelled) {
        // nothing to do here
        Callback.log("Todo editing cancelled")
      } else {
        Callback.log(s"Todo edited: $poi") >>
          $.props >>= (_.proxy.dispatch(UpdatePoi(poi)))
      }
      // hide the edit dialog, chain callbacks
      cb >> $.modState(s => s.copy(showTodoForm = false))
    }

    def render(p: Props, s: State) =
      Panel(Panel.Props("What needs to be done"), <.div(
        p.proxy().renderFailed(ex => "Error loading"),
        p.proxy().renderPending(_ > 500, _ => "Loading..."),
        p.proxy().render(todos => PoiList(todos.pois, item => p.proxy.dispatch(UpdatePoi(item)),
          item => editTodo(Some(item)), item => p.proxy.dispatch(DeletePoi(item)))),
        Button(Button.Props(editTodo(None)), Icon.plusSquare, " New")),
        // if the dialog is open, add it to the panel
        if (s.showTodoForm) PoiForm(PoiForm.Props(s.selectedItem, todoEdited))
        else // otherwise add an empty placeholder
          Seq.empty[ReactElement])
  }

  // create the React component for To Do management
  val component = ReactComponentB[Props]("TODO")
    .initialState(State()) // initial state from TodoStore
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted(scope.props))
    .build

  /** Returns a function compatible with router location system while using our own props */
  def apply(proxy: ModelProxy[Pot[Pois]]) = component(Props(proxy))
}

object PoiForm {
  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class Props(poi: Option[Poi], submitHandler: (Poi, Boolean) => Callback)

  case class State(poi: Poi, cancelled: Boolean = true)

  class Backend(t: BackendScope[Props, State]) {
    def submitForm(): Callback = {
      // mark it as NOT cancelled (which is the default)
      t.modState(s => s.copy(cancelled = false))
    }

    def formClosed(state: State, props: Props): Callback =
      // call parent handler with the new item and whether form was OK or cancelled
      props.submitHandler(state.poi, state.cancelled)

    def updateName(e: ReactEventI) = {
      val text = e.target.value
      // update Poi content
      t.modState(s => s.copy(poi = s.poi.copy(name = text)))
    }

    def render(p: Props, s: State) = {
      log.debug(s"User is ${if (s.poi.id == -1) "adding" else "editing"} a todo or two")
      val headerText = if (s.poi.id == -1) "Add new point of interest" else "Edit point of interest"
      Modal(Modal.Props(
        // header contains a cancel button (X)
        header = hide => <.span(<.button(^.tpe := "button", bss.close, ^.onClick --> hide, Icon.close), <.h4(headerText)),
        // footer has the OK button that submits the form before hiding it
        footer = hide => <.span(Button(Button.Props(submitForm() >> hide), "OK")),
        // this is called after the modal has been hidden (animation is completed)
        closed = formClosed(s, p)),
        <.div(bss.formGroup,
          <.label(^.`for` := "name", "Name"),
          <.input.text(bss.formControl, ^.id := "name", ^.value := s.poi.name,
            ^.placeholder := "write description", ^.onChange ==> updateName))
      )
    }
  }

  val component = ReactComponentB[Props]("PoiForm")
    .initialState_P(p => State(p.poi.getOrElse(Poi(0, ""))))
    .renderBackend[Backend]
    .build

  def apply(props: Props) = component(props)
}