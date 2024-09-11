import logo from './logo.svg';
import './App.css';
import React from 'react'; // remeber to import this


function App() {
  // state variable , used to remmeber things across renders

  const [message, setMessage] = React.useState(null);


  function handleSubmit() {
    // alert("button clicked");

    console.log('button has been clicked')
    // make http call to spark


    // object without needing to make a class


    const body = {
      firstName: 'Brian',
      lastName: 'Parra',

    };

    const options = {
      method: 'post',
      body: JSON.stringify(body),
    };

    fetch('/api/postTest', options)
      .then(res => res.json())// chain what happens after results comeback from 
      .then(data => {
        setMessage(data.message);
      })
  }


  return (
    <div className="App">
      <input />
      <input />
      <button onClick={handleSubmit} >Submit</button>
      < h1> {message}</h1>

    </div>
  );
}

export default App;
