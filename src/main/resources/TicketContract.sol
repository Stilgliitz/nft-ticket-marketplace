//SPDX-License-Identifier: MIT
pragma solidity ^0.8.26;

import "@openzeppelin/contracts/token/ERC721/extensions/ERC721URIStorage.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

contract TicketContract is ERC721URIStorage, Ownable {
    uint256 private nextTokenId;

    constructor() ERC721("EventTickets", "ETK") Ownable(msg.sender) {}

    function mintTicket(address recipient, string memory tokenURI) public onlyOwner returns (uint256) {
        uint256 tokenId = nextTokenId;
        _safeMint(recipient, tokenId);
        _setTokenURI(tokenId, tokenURI);
        nextTokenId++;
        return tokenId;
    }

    function getNextTokenId() public view returns (uint256) {
        return nextTokenId;
    }
}
