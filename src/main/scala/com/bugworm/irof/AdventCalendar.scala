package com.bugworm.irof

import AdventCalendar._
import javafx.application.Application
import javafx.scene.text.FontWeight
import scalafx.Includes._
import scalafx.animation.KeyFrame
import scalafx.animation.Timeline
import scalafx.beans.binding.NumberBinding.sfxNumberBinding2jfx
import scalafx.beans.property.DoubleProperty
import scalafx.event.ActionEvent
import scalafx.geometry.BoundingBox.sfxBoundingBox2jfx
import scalafx.geometry.BoundingBox
import scalafx.scene.canvas.Canvas
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.control.Button
import scalafx.scene.image.Image.sfxImage2jfx
import scalafx.scene.image.Image
import scalafx.scene.input.MouseEvent.sfxMouseEvent2jfx
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.StackPane.sfxStackPane2jfx
import scalafx.scene.layout.StackPane
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Font
import scalafx.scene.ImageCursor
import scalafx.scene.Scene
import scalafx.stage.Stage.sfxStage2jfx
import scalafx.stage.Stage
import scalafx.geometry.Pos
import scalafx.scene.layout.HBox
import scalafx.scene.layout.BorderPane

object AdventCalendar {

    val backimg = new Image("/back.png")
    val balloonimg = new Image("/balloon.png")
    val irofimg = new Image("/irof.png")
    val cursorimg = new Image("/cursor.png")

    val screenWidth = 1000
    val screenHeight = 750
    val screen = Rectangle(0, 0, screenWidth, screenHeight)

    val irofX = screenWidth - irofimg.width.value
    val irofY = screenHeight - irofimg.height.value

    val bomber = DoubleProperty(0)

    val mainloop = new Timeline{
        cycleCount = Timeline.INDEFINITE
    }

    val startbutton = new Button("[START]"){
        font = Font.font("monospaced", FontWeight.BOLD, 48)
        onAction = {e : ActionEvent =>
            visible = false
            mainloop.play
        }
    }

    def main(args : Array[String]) = Application.launch(classOf[AdventCalendar])
}

class AdventCalendar extends Application {

    var balloons = List[Balloon]()
    var enemies = List[Enemy]()
    var mouse : Option[MouseEvent] = None
    var count = 0

    def start(stage: javafx.stage.Stage): Unit = {

        val canvas = new Canvas(screenWidth, screenHeight)
        new Stage(stage){
            scene = new Scene(new StackPane{
	            onMousePressed = {e : MouseEvent => mouse = Option(e)}
	            onMouseDragged = {e : MouseEvent => mouse = Option(e)}
	            onMouseReleased = {mouse = None}
                content = Seq(canvas, new BorderPane{
                    center = startbutton
                    bottom = new HBox{
	                    content = Seq(new Rectangle{
			                fill = Color.LIGHTGREEN
			                width <== bomber * 8
			                height = 50
	                    },
	                    new Rectangle{
	                        fill = Color.RED
	                        width <== (- bomber + 100) * 8
	                        height = 50
	                    })
                    }
                })
                cursor = new ImageCursor(cursorimg, cursorimg.getWidth / 2, cursorimg.getHeight / 2)
            }, screenWidth, screenHeight)
        }.show

        canvas.graphicsContext2D.drawImage(backimg, 0, 0)
        mainloop.keyFrames = KeyFrame(30 ms, "[irof]", runFrame(canvas.graphicsContext2D))
    }

    def runFrame(gc : GraphicsContext): Unit = {
        execute()
        draw(gc)
    }
    def execute(): Unit = {
        count += 1
        balloons.foreach{ _.move() }
        mouse.foreach{e =>
            if(count % 3 == 0){
	            val mx =  e.getX - irofX - balloonimg.getWidth / 2
	            val my  = e.getY - irofY - balloonimg.getHeight / 2
	            val len = Math.sqrt(mx * mx + my * my)
	            balloons = new Balloon(irofX, irofY, mx * 16 / len, my * 16 / len, balloonimg) :: balloons
            }
        }
        if(Math.random > 0.7){
            enemies = new Meteo :: enemies
        }
        enemies.foreach{e =>
            e.move()
            val i = balloons.indexWhere{ b => b.active && b.bounds.intersects(e.bounds) }
            if(i >= 0){
            	balloons(i).active = false
            	e.hp -= 1
            	if(bomber.value < 100)bomber.value = bomber.value + 1
            }
            if(e.crash){
                bomber.value = bomber.value - 1
                e.hp = 0
                //TODO
                if(bomber.value < 0)bomber.value = 0
            }
        }
        balloons = balloons.filter{ _.active }
        enemies = enemies.filter{ _.active }
    }
    def draw(gc : GraphicsContext): Unit = {
        gc.drawImage(backimg, 0, 0)
        enemies.foreach{ ci => gc.drawImage(ci.image, ci.x, ci.y) }
        balloons.foreach{ ci => gc.drawImage(ci.image, ci.x, ci.y) }
        gc.drawImage(irofimg, irofX, irofY)
    }
}

class Balloon(
        var x : Double,
        var y : Double,
        var vx : Double,
        var vy : Double,
        val image : Image){

    var active = true

    def bounds = new BoundingBox(x + 10, y + 10, image.width.value - 20, image.height.value - 20)
    def move() = {
        x += vx
        y += vy
    }
    def inside() : Boolean = {
        screen.intersects(bounds)
    }
}
