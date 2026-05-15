import './App.css';
import Header from './components/Header';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import Collection from "./components/Collection";
import Login from "./components/Login";
import Trading from "./components/Trading"
import PackOpening from "./components/PackOpening"
import { UserProvider } from './context/UserContext';

function App(): JSX.Element {
  return (
    <UserProvider>
      <Router>
        <div>
          <Header />
          <Routes>
            <Route path="/" element={<Navigate to="/collection" replace />} />
            <Route path="/collection" element={<Collection/>} />
            <Route path="/login" element={<Login/>} />
            <Route path="/trading" element={<Trading/>} />
            <Route path="/pack-opening" element={<PackOpening/>}/>
          </Routes>
        </div>
      </Router>
    </UserProvider>
  )
}

export default App;
