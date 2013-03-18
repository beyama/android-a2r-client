package eu.addicted2random.a2rclient.grid;

import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.addicted2random.a2rclient.osc.Pack;

/**
 * Implementation of an {@link Element} that creates a {@link TextView} in the
 * grid.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public class TextElement extends Element<TextView> {

  private static final long serialVersionUID = 9105328376830009642L;

  private String text;

  private Float fontSize;

  private Integer color;

  @JsonCreator
  public TextElement(@JsonProperty("type") String type, @JsonProperty("x") int x, @JsonProperty("y") int y,
      @JsonProperty("cols") int cols, @JsonProperty("rows") int rows) {
    super(type, x, y, cols, rows);
  }

  @Override
  protected TextView createInstance(Context context) {
    return new TextView(context);
  }

  @Override
  protected void setupView() {
    super.setupView();

    TextView view = getView();

    if (text != null)
      view.setText(text);
    if (color != null)
      view.setTextColor(color);
    if (fontSize != null)
      view.setTextSize(fontSize);
  }

  @Override
  protected Pack createPack(ReentrantLock lock) {
    return null;
  }

  @Override
  protected void onSync() {
  }

  /**
   * Get text.
   * 
   * @return
   */
  public String getText() {
    return text;
  }

  /**
   * Set text.
   * 
   * @param text
   */
  @JsonProperty
  public void setText(String text) {
    this.text = text;
  }

  /**
   * Get font size.
   * 
   * @return
   */
  public Float getFontSize() {
    return fontSize;
  }

  /**
   * Set font size.
   * 
   * @param fontSize
   */
  @JsonProperty
  public void setFontSize(Float fontSize) {
    this.fontSize = fontSize;
  }

  /**
   * Get color.
   * 
   * @return
   */
  public Integer getColor() {
    return color;
  }

  /**
   * Set color.
   * 
   * @param color
   */
  @JsonProperty
  public void setColor(String color) {
    this.color = Color.parseColor(color);
  }

}
