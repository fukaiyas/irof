import scalafx.stage.Stage
import scalafx.stage.Stage._
import scalafx.scene._
import scalafx.scene.input._
import scalafx.scene.shape._
import scalafx.scene.paint._
import scalafx.scene.shape._
import scalafx.Includes._
import javafx.application.Application

object IrofShape{
    def main(args : Array[String]) = Application.launch(classOf[IrofShape])
}
class IrofShape extends Application {

    def start(stage: javafx.stage.Stage): Unit = {
        val irof = new Group{
            children = Seq(
                //枠
                new Rectangle{
                    strokeWidth = 5
                    stroke = Color.BLACK
                    fill = Color.WHITE
                    translateX = 0
                    translateY = 0
                    width = 300
                    height = 300
                },
                //輪郭
                new Path{
                    strokeWidth = 10
                    stroke = Color.BLACK
                    fill.value = null
                    elements = Seq(
                        MoveTo(126.5, 267),
                        ArcTo(117, 117, 0, 146, 184.5, true, false),
                        LineTo(210, 255),
                        new ClosePath
                    )
                    clip = Rectangle(0, 0, 300, 300)
                },
                //ふき出し
                new Path{
                    strokeWidth = 10
                    stroke = Color.BLACK
                    fill = Color.WHITE
                    elements = Seq(
                        MoveTo(50, 30),
                        LineTo(153, 30),
                        ArcTo(30, 30, 0, 153, 90, false, true),
                        LineTo(105, 90),
                        CubicCurveTo(105, 90, 90, 105, 129, 142),
                        CubicCurveTo(90, 135, 66, 120, 81, 90),
                        LineTo(57, 90),
                        ArcTo(30, 30, 0, 50, 30, false, true)
                    )
                },
                //目
                new Circle{
                    centerX = 255
                    centerY = 204
                    radius = 15
                    fill = Color.WHITE
                    stroke = Color.BLACK
                    strokeWidth = 10
                }
            //ふき出しの中の点
            ) ++ (for(x <- Seq(51, 84, 120, 154))yield Circle(x, 60, 5, Color.BLACK))
        }
        new Stage(stage){
            title = "irof shape by ScalaFX"
            scene = new Scene(irof, 300, 300)
        }.show
    }
}
