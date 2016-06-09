import React from 'react';
import Table from '../common/Table';
import PlainText from '../common/atomicDisplayItems/PlainText';
import Glyphicon from '../common/atomicDisplayItems/Glyphicon';
import Timestamp from '../common/atomicDisplayItems/Timestamp';
import Utils from '../../utils';

export default class StatusList extends React.Component {

  constructor() {
    super();
    this.state = {
      showChange: false,
      changes: []
    };
  }

  componentWillReceiveProps(nextProps) {
    let changes = [];
    for (let d of nextProps.data) {
      if (!d || !d.prop.id) return;
      let matchingData = _.find(this.props.data, (data) => {
        return data.id == d.id;
      });
      console.log(d.prop.value, matchingData.prop.value);
      if (d.prop.value && matchingData) {
        changes.push({
          id: d.id,
          diff: d.prop.value - matchingData.prop.value
        });
      }
    }
    console.log(changes);
    this.setState({
      showChange: changes.length > 0,
      changes: changes
    });
  }

  renderBefore(d) {
    if (d.beforeFill) {
      return (
        <span className={`chart__legend-fill chart-fill-${d.beforeFill}`}></span>
      );
    }
  }

  renderItems() {
    return this.props.data.map((d, i) => {
      if (!d) {return}
      let ComponentClass = d.component;
      return (
        <li key={i} className="list-group-item">
          {this.renderBefore(d)}
          <ComponentClass prop={d.prop} />
        </li>
      );
    });
  }

  render() {
    return (
      <div>
          <h2>{this.props.header}</h2>
          <ul className="list-group list-group--links list-group--plain list-group--slim list-large">{this.renderItems()}</ul>
      </div>
    );
  }
}

StatusList.propTypes = {
  header: React.PropTypes.string,
  data: React.PropTypes.arrayOf(React.PropTypes.shape({
      component: React.PropTypes.func.isRequired,
      value: React.PropTypes.number,
      prop: React.PropTypes.object,
      id: React.PropTypes.string,
      className: React.PropTypes.string,
      beforeFill: React.PropTypes.string
  })).isRequired
};