import logo from './logo.svg';
import './App.css';
import React from 'react';
function App() {
  // state variables
  const [userName,setUsername] = React.useState('');
  const [message, setMessage] = React.useState(''); // current typing
  const [messages,setMessages] = React.useState([]);// all messages
  const [LoggedIn, setIsLoggedIn] = React.useState(false);

  function getAllMessages(){
    fetch('/getMessages')
    .then(res => res.json())
    .then(data => {
      console.log(data);
      setMessages(data);
    })
  }

  function submitNewMessage(){
    console.log(message);// browser's console
    setMessage(''); // clear text in the text area
    //send to spark
    const body = {
      userName: userName,
      message: message,
    }
    const settings = {
      method: 'post',
      body: JSON.stringify(body),
    };
    fetch('/submitMessage', settings)// builder style syntax
    .then(() => getAllMessages())
    .catch(e => console.log(e));
    //keep at the end
    setMessage(''); // clear text in the text area
   // getAllMessages();
  }

function login(){
  getAllMessages();
  setIsLoggedIn(true)
}

  if(!LoggedIn){
    return(
      <div >
    	  <h1> Please Log in</h1>
        <input value = {userName} onChange= {(event) => setUsername(event.target.value)}/>
        <button onClick = {login}> Log in</button>
      </div>
    );
  }

  const messageStyle = {
    backgroundColor: 'rgb(240,240,240)',
    marginBottom: '10px'
  }
  
  return (
    <div className="App">
    	<h1>Welcome {userName}</h1>
      <textarea value = {message} onChange= {(event) => setMessage(event.target.value)} />
      <button onClick= {submitNewMessage}> Submit </button>

      <div>

      {messages.map(m =>{
        return(
            // how to render each message one at a time
            <div style = {messageStyle}>
              username: {m.userName}
              <br/>
              message: {m.message}

            </div>
        );
      })}

      </div>
    </div>


  );
}

export default App;
