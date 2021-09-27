from bs4 import BeautifulSoup
from os.path import dirname, join
from com.chaquo.python import Python
import requests
import pickle


def main(username, password, school_id):
    try:
        r = login_with_cookies(school_id)
        timetable_grab = format_html(r.text)
        return timetable_grab
    except:
        try:
            r = login(username, password, school_id)
        except:
            return "network_error"

        try:
            timetable_grab = format_html(r.text)
            return timetable_grab
        except:
            return "error"


def login(username, password, school_id):
    login_url = 'https://' + school_id + '.edookit.net/user/login'
    login_check_url = 'https://' + school_id + '.edookit.net/user/login?do=loginForm-checkCaptcha&loginForm-username=' + username
    home_url = 'https://' + school_id + '.edookit.net/'

    payload = {
        'username': username,
        'password': password,
        'g-recaptcha-response': '',
        'send': 'Přihlásit',
        'remember': '1',
        '_do': 'loginForm-form-submit'
    }

    with requests.session() as s:
        s.get(login_check_url)
        s.post(login_url, data=payload)
        r = s.get(home_url)

        files_dir = str(Python.getPlatform().getApplication().getFilesDir())
        file_name = join(dirname(files_dir), 'cookies')

        with open(file_name, 'wb') as f:
            pickle.dump(s.cookies, f)

        return r


def login_with_cookies(school_id):
    home_url = 'https://' + school_id + '.edookit.net/'

    with requests.session() as s:
        files_dir = str(Python.getPlatform().getApplication().getFilesDir())
        file_name = join(dirname(files_dir), 'cookies')

        with open(file_name, 'rb') as f:
            s.cookies.update(pickle.load(f))
        r = s.get(home_url)

    return r


def getFullname(username, password, school_id):
    try:
        r = login(username, password, school_id)
    except:
        return "network_error"

    try:
        soup = BeautifulSoup(r.text, 'html.parser')
        return soup.find(class_='fullname').text.strip()
    except:
        return "error"


def format_html(html):
    # Cut out timetable
    soup = BeautifulSoup(BeautifulSoup(html, 'html.parser').find(class_='timetable-container').prettify(),
                         'html.parser')
    # Force width to 900px
    soup.find(class_='timetable-container')['style'] = 'max-width:900px;display: block'
    # Remove java scripts
    for e in soup.select('script'):
        e.extract()
    # Remove timeline
    for div in soup.find_all("div", {'class': 'hideInPrint'}):
        div.decompose()
    # Set viewport; Remove min-width; Resize fonts; Remove backgrounds
    html = '<meta name="viewport" content="width=device-width, initial-scale=1">\n' + soup.prettify().replace(
        '\n        min-width: 1258px;', '').replace('font-size:80', 'font-size:95').replace(
        'background-color: #f7f7f7;', '')
    return html
